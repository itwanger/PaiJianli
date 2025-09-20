package com.yizhaoqi.pairesume.vo;

import com.yizhaoqi.pairesume.common.enums.LogType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PayLogVO {
    private Long id;
    private String payOrderId;
    private LogType logType;
    private String content;
    private LocalDateTime createdTime;
}
