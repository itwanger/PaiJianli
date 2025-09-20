package com.yizhaoqi.pairesume.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PayCreateVO {
    private String payOrderId;
    private Map<String, String> payParams;
}
