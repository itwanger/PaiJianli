package com.yizhaoqi.pairesume.common.enums;

import lombok.Getter;

@Getter
public enum PayStatus {
    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败");

    private final int value;
    private final String description;

    PayStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
