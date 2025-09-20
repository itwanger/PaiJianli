package com.yizhaoqi.pairesume.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yizhaoqi.pairesume.common.enums.LogType;
import com.yizhaoqi.pairesume.common.enums.PayStatus;
import com.yizhaoqi.pairesume.common.exception.ServiceException;
import com.yizhaoqi.pairesume.config.AlipayConfig;
import com.yizhaoqi.pairesume.dto.PayCreateDTO;
import com.yizhaoqi.pairesume.dto.PayCreateVO;
import com.yizhaoqi.pairesume.dto.PayLogVO;
import com.yizhaoqi.pairesume.dto.PayStatusVO;
import com.yizhaoqi.pairesume.entity.PayLog;
import com.yizhaoqi.pairesume.entity.PayOrder;
import com.yizhaoqi.pairesume.repository.PayLogRepository;
import com.yizhaoqi.pairesume.repository.PayOrderRepository;
import com.yizhaoqi.pairesume.service.IPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements IPayService {

    private final AlipayConfig alipayConfig;
    private final AlipayClient alipayClient;
    private final PayOrderRepository payOrderRepository;
    private final PayLogRepository payLogRepository;
    private final ObjectMapper objectMapper;


    /**
     * 创建支付订单
     * 遵循官方文档，同时返回PC和H5的支付参数
     */
    @Override
    @Transactional
    public PayCreateVO createPayment(PayCreateDTO payCreateDTO, Long userId) {
        // TODO: [高优先级安全问题] 此处必须根据 bizOrderId 和 bizType 从业务模块查询原始订单，
        //  进行金额、订单状态和用户归属权的严格校验，不能信任任何前端传入的金额。
        //  例如: BusinessOrder bo = businessOrderService.getOrderByBizId(payCreateDTO.getBizOrderId());
        //  if (!bo.getUserId().equals(userId) || bo.getAmount().compareTo(payCreateDTO.getAmount()) != 0) {
        //      throw new ServiceException("订单信息校验失败");
        //  }

        // 检查是否存在统一业务订单的待支付或已支付订单，防止重复创建
        payOrderRepository.findByBizOrderIdAndBizType(payCreateDTO.getBizOrderId(), payCreateDTO.getBizType())
                .ifPresent(existingOrder -> {
                    if (existingOrder.getStatus() != PayStatus.FAILED) {
                        throw new ServiceException("该业务订单已存在支付记录，请勿重复创建");
                    }
                });

        // 1. 创建并保存支付订单实体
        PayOrder payOrder = createAndSavePayOrder(payCreateDTO, userId);
        String payOrderId = payOrder.getPayOrderId();

        // 2. 构造支付请求参数的公共部分
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", payOrderId);
        bizContent.put("total_amount", payOrder.getAmount().toString());
        bizContent.put("subject", payCreateDTO.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 统一使用标准产品码

        try {
            String bizContentJson = objectMapper.writeValueAsString(bizContent);
            Map<String, String> payParams = new HashMap<>();

            // 3. 生成H5支付参数
            AlipayTradeWapPayRequest wapRequest = new AlipayTradeWapPayRequest();
            wapRequest.setNotifyUrl(alipayConfig.getDomain() + "/pay/callback");
            wapRequest.setBizContent(bizContentJson);
            String wapForm = alipayClient.pageExecute(wapRequest).getBody();
            payParams.put("h5", wapForm);

            // 4. 生成PC扫码支付参数
            AlipayTradePrecreateRequest precreateRequest = new AlipayTradePrecreateRequest();
            precreateRequest.setNotifyUrl(alipayConfig.getDomain() + "/pay/callback");
            precreateRequest.setBizContent(bizContentJson);
            String precreateResponse = alipayClient.execute(precreateRequest).getBody();
            payParams.put("pc", precreateResponse); // 返回的是包含二维码链接的JSON字符串

            // 5. 记录请求日志
            savePayLog(payOrderId, LogType.REQUEST, bizContentJson, null);

            return PayCreateVO.builder()
                    .payOrderId(payOrderId)
                    .payParams(payParams)
                    .build();

        } catch (AlipayApiException | JsonProcessingException e) {
            log.error("创建支付宝支付订单失败, payOrderId={}", payOrderId, e);
            throw new ServiceException("创建支付订单失败，请稍后重试");
        }
    }

    /**
     * 处理支付宝回调
     */
    @Override
    @Transactional
    public String handleAlipayCallback(Map<String, String> params) {
        log.info("接收到支付宝异步回调: {}", params);
        final String successResponse = "success";
        final String failureResponse = "failure";

        try {
            // 1. 签名校验
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), alipayConfig.getSignType());
            if (!signVerified) {
                log.warn("支付宝回调签名校验失败, params: {}", params);
                savePayLog(params.get("out_trade_no"), LogType.CALLBACK, "签名校验失败", null);
                return failureResponse;
            }

            // 2. 获取关键参数
            String payOrderId = params.get("out_trade_no");
            String transactionId = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            String totalAmount = params.get("total_amount");

            // 3. 记录回调日志
            savePayLog(payOrderId, LogType.CALLBACK, convertMapToJson(params), transactionId);

            // 4. 业务逻辑校验
            PayOrder payOrder = payOrderRepository.findByPayOrderId(payOrderId)
                    .orElseThrow(() -> new ServiceException("未找到对应的支付订单: " + payOrderId));

            // 4.1 幂等性校验：如果订单已成功，直接返回success
            if (payOrder.getStatus() == PayStatus.SUCCESS) {
                log.info("订单 {} 已处理成功，无需重复处理", payOrderId);
                return successResponse;
            }

            // 4.2 状态校验：只处理支付成功或交易完成的状态
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.warn("支付宝回调状态非成功, trade_status: {}, 不更新订单状态", tradeStatus);
                return successResponse; // 支付宝要求非成功状态也返回success
            }
            
            // 4.3 金额校验
            if (payOrder.getAmount().compareTo(new BigDecimal(totalAmount)) != 0) {
                log.error("支付宝回调金额与订单金额不符! orderAmount={}, callbackAmount={}, payOrderId={}",
                        payOrder.getAmount(), totalAmount, payOrderId);
                // 可在此处加入更复杂的逻辑，如标记为异常订单
                return failureResponse;
            }

            // 5. 更新订单状态
            payOrder.setStatus(PayStatus.SUCCESS);
            payOrder.setTransactionId(transactionId);
            payOrderRepository.save(payOrder);
            log.info("支付订单 {} 状态更新为支付成功", payOrderId);

            // 6. TODO: [核心功能] 异步通知业务方
            //  此处应使用如@Async注解的异步方法，通过RestTemplate或WebClient调用 payOrder.getNotifyUrl()
            //  通知业务方支付成功，并处理通知失败的重试机制。
            //  notifyBusinessService.notify(payOrder.getNotifyUrl(), payOrder);

            return successResponse;

        } catch (Exception e) {
            log.error("处理支付宝回调时发生异常", e);
            return failureResponse;
        }
    }

    @Override
    public PayStatusVO getPaymentStatus(String payOrderId, Long userId) {
        PayOrder payOrder = payOrderRepository.findByPayOrderId(payOrderId)
                .orElseThrow(() -> new ServiceException("支付订单不存在"));

        // 权限校验
        if (!payOrder.getUserId().equals(userId)) {
            throw new ServiceException("无权查询该支付订单");
        }

        return PayStatusVO.builder()
                .payOrderId(payOrder.getPayOrderId())
                .status(payOrder.getStatus())
                .amount(payOrder.getAmount())
                .updatedTime(payOrder.getUpdatedTime())
                .build();
    }

    @Override
    public List<PayLogVO> getPaymentLogs(String payOrderId, LogType logType, Long userId) {
        // 先查询订单，进行权限校验
        PayOrder payOrder = payOrderRepository.findByPayOrderId(payOrderId)
                .orElseThrow(() -> new ServiceException("支付订单不存在"));
        if (!payOrder.getUserId().equals(userId)) {
            throw new ServiceException("无权查询该支付订单的日志");
        }

        List<PayLog> logs;
        if (logType != null) {
            logs = payLogRepository.findByPayOrderIdAndLogType(payOrderId, logType);
        } else {
            logs = payLogRepository.findByPayOrderId(payOrderId);
        }

        return logs.stream().map(this::convertToPayLogVO).collect(Collectors.toList());
    }

    // --- private helper methods ---

    private PayOrder createAndSavePayOrder(PayCreateDTO dto, Long userId) {
        PayOrder order = new PayOrder();
        order.setPayOrderId("pay_" + UUID.randomUUID().toString().replace("-", ""));
        order.setBizOrderId(dto.getBizOrderId());
        order.setBizType(dto.getBizType());
        order.setUserId(userId);
        order.setAmount(dto.getAmount());
        order.setStatus(PayStatus.PENDING);
        order.setNotifyUrl(dto.getNotifyUrl());
        order.setExtra(dto.getExtra());
        return payOrderRepository.save(order);
    }
    
    private void savePayLog(String payOrderId, LogType logType, String content, String transactionId) {
        PayLog payLog = new PayLog();
        payLog.setPayOrderId(payOrderId);
        payLog.setLogType(logType);
        payLog.setContent(content);
        payLog.setTransactionId(transactionId);
        payLogRepository.save(payLog);
    }

    private String convertMapToJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("转换Map到JSON字符串失败", e);
            return "{\"error\":\"Failed to serialize callback map\"}";
        }
    }

    private PayLogVO convertToPayLogVO(PayLog log) {
        return PayLogVO.builder()
                .id(log.getId())
                .payOrderId(log.getPayOrderId())
                .logType(log.getLogType())
                .content(log.getContent())
                .createdTime(log.getCreatedTime())
                .build();
    }
}
