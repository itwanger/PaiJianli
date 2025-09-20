package com.yizhaoqi.pairesume.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PricePriorityDTO {

    @NotNull(message = "加塞等级不能为空")
    private Integer priorityLevel;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String description;

    private Integer status;
}
