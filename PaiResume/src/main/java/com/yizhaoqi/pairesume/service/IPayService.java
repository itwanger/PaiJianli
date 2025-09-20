package com.yizhaoqi.pairesume.service;

import com.yizhaoqi.pairesume.common.enums.LogType;
import com.yizhaoqi.pairesume.dto.PayCreateDTO;
import com.yizhaoqi.pairesume.dto.PayCreateVO;
import com.yizhaoqi.pairesume.dto.PayLogVO;
import com.yizhaoqi.pairesume.dto.PayStatusVO;

import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface IPayService {

    /**
     * 创建支付订单
     *
     * @param payCreateDTO 支付订单创建信息
     * @param userId       用户ID
     * @return 支付参数，用于前端拉起支付
     */
    PayCreateVO createPayment(PayCreateDTO payCreateDTO, Long userId);

    /**
     * 处理支付宝异步回调通知
     *
     * @param params 支付宝回调的参数Map
     * @return 处理结果 ("success" or "failure")
     */
    String handleAlipayCallback(Map<String, String> params);

    /**
     * 查询支付状态
     *
     * @param payOrderId 支付订单号
     * @param userId     用户ID
     * @return 支付状态信息
     */
    PayStatusVO getPaymentStatus(String payOrderId, Long userId);

    /**
     * 查询支付日志
     *
     * @param payOrderId 支付订单号
     * @param logType    日志类型 (可选)
     * @param userId     用户ID
     * @return 日志列表
     */
    List<PayLogVO> getPaymentLogs(String payOrderId, LogType logType, Long userId);
}
