package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.PayOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayOrderRepository extends JpaRepository<PayOrder, Long> {

    /**
     * 根据支付订单号查询
     * @param payOrderId 支付订单号
     * @return 支付订单
     */
    Optional<PayOrder> findByPayOrderId(String payOrderId);

    /**
     * 根据业务订单号和业务类型查询
     * @param bizOrderId 业务订单号
     * @param bizType 业务类型
     * @return 支付订单
     */
    Optional<PayOrder> findByBizOrderIdAndBizType(String bizOrderId, String bizType);

    /**
     * 根据支付宝交易号查询
     * @param transactionId 支付宝交易号
     * @return 支付订单
     */
    Optional<PayOrder> findByTransactionId(String transactionId);
}
