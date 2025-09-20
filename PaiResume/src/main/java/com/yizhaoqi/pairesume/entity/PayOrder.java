package com.yizhaoqi.pairesume.entity;

import com.yizhaoqi.pairesume.common.enums.PayStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pay_order")
public class PayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pay_order_id", nullable = false, unique = true, length = 64)
    private String payOrderId;

    @Column(name = "biz_order_id", nullable = false, length = 64)
    private String bizOrderId;

    @Column(name = "biz_type", nullable = false, length = 32)
    private String bizType;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private PayStatus status = PayStatus.PENDING;

    @Column(name = "pay_channel", nullable = false, length = 32)
    private String payChannel = "alipay";

    @Column(name = "notify_url", length = 255)
    private String notifyUrl;

    @Column(name = "transaction_id", length = 64)
    private String transactionId;

    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @Column(columnDefinition = "json")
    private String extra;
}
