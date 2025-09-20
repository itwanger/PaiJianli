package com.yizhaoqi.pairesume.dto;

import com.yizhaoqi.pairesume.common.enums.PayStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PayStatusVO {
    private String payOrderId;
    private PayStatus status;
    private BigDecimal amount;
    private LocalDateTime updatedTime;
}
