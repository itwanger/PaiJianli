package com.yizhaoqi.pairesume.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 统一的通知视图对象，用于返回给前端
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationVO {
    private Long id;
    private String type; // "SYSTEM" 或 "SERVICE"
    private String title;
    private String content;
    private String status;
    private Timestamp createdAt;
}
