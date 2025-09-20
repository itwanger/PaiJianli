package com.yizhaoqi.pairesume.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayCreateDTO {
    private String bizOrderId;
    private String bizType;
    private BigDecimal amount;
    private String subject;
    private String notifyUrl;
    private String extra;
}
