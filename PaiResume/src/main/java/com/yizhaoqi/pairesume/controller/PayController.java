package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.common.enums.LogType;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.common.utils.RequestUtils;
import com.yizhaoqi.pairesume.dto.PayCreateDTO;
import com.yizhaoqi.pairesume.vo.PayCreateVO;
import com.yizhaoqi.pairesume.vo.PayLogVO;
import com.yizhaoqi.pairesume.vo.PayStatusVO;
import com.yizhaoqi.pairesume.service.IPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final IPayService payService;
    private final JwtUtil jwtUtil;

    /**
     * 1. 创建支付订单
     * @param payCreateDTO 业务方传递的订单信息
     * @return 包含支付参数的对象
     */
    @PostMapping("/create")
    public R<PayCreateVO> createPayment(@RequestBody PayCreateDTO payCreateDTO, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        PayCreateVO paymentResult = payService.createPayment(payCreateDTO, userIdOptional.get());
        return R.ok(paymentResult);
    }

    /**
     * 2. 支付宝异步回调
     * 注意：这是一个开放接口，由支付宝调用，不能添加认证拦截
     * @param request 包含支付宝回调参数的请求
     * @return "success" 或 "failure"
     */
    @PostMapping("/callback")
    public String handleAlipayCallback(HttpServletRequest request) {
        // 将请求参数转换为Map
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            String valueStr = (values != null && values.length > 0) ? values[0] : "";
            params.put(key, valueStr);
        });
        return payService.handleAlipayCallback(params);
    }

    /**
     * 3. 查询支付状态
     * @param payOrderId 支付系统内部订单号
     * @return 支付状态信息
     */
    @GetMapping("/status")
    public R<PayStatusVO> getPaymentStatus(@RequestParam("pay_order_id") String payOrderId, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        PayStatusVO statusResult = payService.getPaymentStatus(payOrderId, userIdOptional.get());
        return R.ok(statusResult);
    }

    /**
     * 4. 查询支付日志
     * @param payOrderId 支付系统内部订单号
     * @param logType 日志类型 (可选)
     * @return 支付日志列表
     */
    @GetMapping("/logs")
    public R<List<PayLogVO>> getPaymentLogs(@RequestParam("pay_order_id") String payOrderId,
                                            @RequestParam(value = "log_type", required = false) LogType logType,
                                            HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        List<PayLogVO> logsResult = payService.getPaymentLogs(payOrderId, logType, userIdOptional.get());
        return R.ok(logsResult);
    }
}
