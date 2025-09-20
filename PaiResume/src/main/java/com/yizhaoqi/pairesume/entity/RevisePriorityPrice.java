package com.yizhaoqi.pairesume.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 简历修改加塞价格表实体
 */
@Data
@Entity
@Table(name = "revise_priority_price")
public class RevisePriorityPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "priority_level", unique = true, nullable = false)
    private Integer priorityLevel;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    private Integer status;
}
