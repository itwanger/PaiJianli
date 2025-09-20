package com.yizhaoqi.pairesume.entity;

import com.yizhaoqi.pairesume.common.enums.LogType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pay_log")
public class PayLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pay_order_id", nullable = false, length = 64)
    private String payOrderId;

    @Column(name = "log_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private LogType logType;

    @Column(columnDefinition = "json")
    private String content;

    @Column(name = "transaction_id", length = 64)
    private String transactionId;

    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
}
