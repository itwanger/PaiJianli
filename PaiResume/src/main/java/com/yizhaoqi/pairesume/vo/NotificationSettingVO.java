package com.yizhaoqi.pairesume.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户通知设置视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingVO {
    private boolean systemEmailSubscribe;
    private boolean serviceEmailSubscribe;
}
