package com.yizhaoqi.pairesume.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceBaseDTO {

    @NotNull(message = "使用次数不能为空")
    private Integer usageCount;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String description;

    private Integer status;
}
