package com.yizhaoqi.pairesume.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 简历修改基础价格表实体
 */
@Data
@Entity
@Table(name = "revise_base_price")
public class ReviseBasePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usage_count", unique = true, nullable = false)
    private Integer usageCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    private Integer status;
}
