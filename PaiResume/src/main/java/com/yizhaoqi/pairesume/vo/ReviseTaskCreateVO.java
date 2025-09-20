package com.yizhaoqi.pairesume.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ReviseTaskCreateVO {

    private Long taskId;
    private String bizOrderId;
    private BigDecimal amount;
    private Integer status;
    
    /**
     * 如果需要支付，这里会包含支付参数 (例如，PC端的支付二维码链接，H5端的支付表单)
     */
    private Map<String, String> payParams;
}
