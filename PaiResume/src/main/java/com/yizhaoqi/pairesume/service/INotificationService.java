package com.yizhaoqi.pairesume.service;

import com.yizhaoqi.pairesume.dto.NotificationSettingDTO;
import com.yizhaoqi.pairesume.dto.SystemNotificationSendDTO;
import com.yizhaoqi.pairesume.vo.NotificationSettingVO;
import com.yizhaoqi.pairesume.vo.NotificationVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INotificationService {

    /**
     * 获取当前用户的通知列表（聚合个人和系统通知）
     *
     * @param userId   当前用户ID
     * @param pageable 分页参数
     * @return 分页后的通知列表
     */
    Page<NotificationVO> getNotifications(Long userId, Pageable pageable);

    /**
     * 将单条通知标记为已读
     *
     * @param userId         当前用户ID
     * @param notificationId 通知ID
     * @param type           通知类型 ("SYSTEM" 或 "SERVICE")
     */
    void markAsRead(Long userId, Long notificationId, String type);

    /**
     * 将当前用户的所有未读通知标记为已读
     *
     * @param userId 当前用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 获取用户通知设置
     *
     * @param userId 当前用户ID
     * @return 通知设置
     */
    NotificationSettingVO getNotificationSettings(Long userId);

    /**
     * 更新用户通知设置
     *
     * @param userId  当前用户ID
     * @param settingDTO 设置参数
     */
    void updateNotificationSettings(Long userId, NotificationSettingDTO settingDTO);

    /**
     * (管理员) 发送系统通知
     *
     * @param sendDTO    发送参数
     * @param operatorId 操作员ID
     */
    void sendSystemNotification(SystemNotificationSendDTO sendDTO, Long operatorId);
}
