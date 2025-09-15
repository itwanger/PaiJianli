package com.yizhaoqi.pairesume.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员发送系统通知DTO
 */
@Data
public class SystemNotificationSendDTO {
    private String type;
    private String title;
    private String content;
    private List<String> sendChannel;
    private List<Long> targetUserIds;
}
