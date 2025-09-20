package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.common.enums.LogType;
import com.yizhaoqi.pairesume.entity.PayLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayLogRepository extends JpaRepository<PayLog, Long> {

    /**
     * 根据支付订单号查询日志
     * @param payOrderId 支付订单号
     * @return 日志列表
     */
    List<PayLog> findByPayOrderId(String payOrderId);

    /**
     * 根据支付订单号和日志类型查询日志
     * @param payOrderId 支付订单号
     * @param logType 日志类型
     * @return 日志列表
     */
    List<PayLog> findByPayOrderIdAndLogType(String payOrderId, LogType logType);
}
