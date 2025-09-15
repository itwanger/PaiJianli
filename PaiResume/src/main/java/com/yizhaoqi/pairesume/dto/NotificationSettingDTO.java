package com.yizhaoqi.pairesume.dto;

import lombok.Data;

/**
 * 用户更新通知设置DTO
 */
@Data
public class NotificationSettingDTO {
    private Boolean systemEmailSubscribe;
    private Boolean serviceEmailSubscribe;
}
