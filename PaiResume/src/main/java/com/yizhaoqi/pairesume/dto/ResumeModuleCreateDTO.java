package com.yizhaoqi.pairesume.dto;

import lombok.Data;

@Data
public class ResumeModuleCreateDTO {
    private String moduleType;
    private String content; // 接收 JSON 字符串
    private Integer sortOrder;
}
