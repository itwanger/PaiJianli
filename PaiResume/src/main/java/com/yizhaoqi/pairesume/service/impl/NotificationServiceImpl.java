package com.yizhaoqi.pairesume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yizhaoqi.pairesume.dto.NotificationSettingDTO;
import com.yizhaoqi.pairesume.dto.SystemNotificationSendDTO;
import com.yizhaoqi.pairesume.entity.SystemNotification;
import com.yizhaoqi.pairesume.entity.User;
import com.yizhaoqi.pairesume.entity.UserSystemNotificationRead;
import com.yizhaoqi.pairesume.kafka.producer.NotificationProducer;
import com.yizhaoqi.pairesume.repository.SystemNotificationRepository;
import com.yizhaoqi.pairesume.repository.UserNotificationRepository;
import com.yizhaoqi.pairesume.repository.UserRepository;
import com.yizhaoqi.pairesume.repository.UserSystemNotificationReadRepository;
import com.yizhaoqi.pairesume.service.INotificationService;
import com.yizhaoqi.pairesume.vo.NotificationSettingVO;
import com.yizhaoqi.pairesume.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yizhaoqi.pairesume.entity.UserNotification;
import com.fasterxml.jackson.core.JsonProcessingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final SystemNotificationRepository systemNotificationRepository;
    private final UserSystemNotificationReadRepository userSystemNotificationReadRepository;
    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper;


    @Override
    public Page<NotificationVO> getNotifications(Long userId, Pageable pageable) {
        // 1. 获取该用户已读的系统通知ID集合，用于后续判断状态
        Set<Long> readSystemNotificationIds = userSystemNotificationReadRepository.findSystemNotificationIdsByUserId(userId);

        // 2. 获取所有系统通知，并转换为VO
        List<NotificationVO> systemNotifications = systemNotificationRepository.findAll().stream()
                .map(sn -> {
                    NotificationVO vo = new NotificationVO();
                    vo.setId(sn.getId());
                    vo.setType("SYSTEM");
                    vo.setTitle(sn.getTitle());
                    vo.setContent(sn.getContent());
                    // 如果ID在已读集合中，则为READ，否则为UNREAD
                    vo.setStatus(readSystemNotificationIds.contains(sn.getId()) ? "READ" : "UNREAD");
                    vo.setCreatedAt(sn.getCreatedAt());
                    return vo;
                })
                .collect(Collectors.toList());

        // 3. 获取该用户的个人通知，并转换为VO
        // 注意：这里的查询已经是分页的，但为了统一排序，我们先取出内容
        List<NotificationVO> userNotifications = userNotificationRepository.findByUserId(userId, pageable).getContent().stream()
                .map(un -> {
                    NotificationVO vo = new NotificationVO();
                    vo.setId(un.getId());
                    vo.setType("SERVICE");
                    vo.setTitle(un.getTitle());
                    vo.setContent(un.getContent());
                    vo.setStatus(un.getStatus());
                    vo.setCreatedAt(un.getCreatedAt());
                    return vo;
                })
                .collect(Collectors.toList());

        // 4. 合并两个通知列表
        List<NotificationVO> combinedList = Stream.concat(systemNotifications.stream(), userNotifications.stream())
                // 5. 按创建时间降序排序
                .sorted(Comparator.comparing(NotificationVO::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // 6. 手动在内存中进行分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), combinedList.size());
        List<NotificationVO> pageContent = combinedList.subList(start, end);

        // 7. 创建并返回分页对象
        return new PageImpl<>(pageContent, pageable, combinedList.size());
    }

    @Override
    public void markAsRead(Long userId, Long notificationId, String type) {
        if ("SYSTEM".equalsIgnoreCase(type)) {
            //
            List<UserSystemNotificationRead> notificationList = userSystemNotificationReadRepository.findByUserIdAndSystemNotificationId(userId, notificationId);
            if (!notificationList.isEmpty()) {
                log.warn("User {} has already read system notification {}", userId, notificationId);
                return;
            }
            UserSystemNotificationRead readRecord = new UserSystemNotificationRead();
            readRecord.setUserId(userId);
            readRecord.setSystemNotificationId(notificationId);
            userSystemNotificationReadRepository.save(readRecord);
        } else if ("SERVICE".equalsIgnoreCase(type)) {
            UserNotification notification = userNotificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));
            // 权限校验：确保用户只能修改自己的通知
            if (!notification.getUserId().equals(userId)) {
                throw new SecurityException("User has no permission to update this notification");
            }
            notification.setStatus("READ");
            userNotificationRepository.save(notification);
        } else {
            throw new IllegalArgumentException("Invalid notification type: " + type);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        // 1. 批量更新该用户的所有个人通知为已读
        userNotificationRepository.markAllAsReadByUserId(userId);

        // 2. 将所有未读的系统通知标记为已读
        // a. 找出所有系统通知的ID
        List<Long> allSystemNotificationIds = systemNotificationRepository.findAll().stream()
                .map(com.yizhaoqi.pairesume.entity.SystemNotification::getId)
                .collect(Collectors.toList());

        // b. 找出用户已读的系统通知ID
        Set<Long> readSystemNotificationIds = userSystemNotificationReadRepository.findSystemNotificationIdsByUserId(userId);

        // c. 过滤出用户还未读的系统通知，并创建新的已读记录
        List<UserSystemNotificationRead> newReadRecords = allSystemNotificationIds.stream()
                .filter(id -> !readSystemNotificationIds.contains(id))
                .map(unreadId -> {
                    UserSystemNotificationRead newRead = new UserSystemNotificationRead();
                    newRead.setUserId(userId);
                    newRead.setSystemNotificationId(unreadId);
                    return newRead;
                })
                .collect(Collectors.toList());

        // d. 批量保存新的已读记录
        if (!newReadRecords.isEmpty()) {
            userSystemNotificationReadRepository.saveAll(newReadRecords);
        }
    }

    @Override
    public NotificationSettingVO getNotificationSettings(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return new NotificationSettingVO(user.getSystemEmailSubscribe(), user.getServiceEmailSubscribe());
    }

    @Override
    public void updateNotificationSettings(Long userId, NotificationSettingDTO settingDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (settingDTO.getSystemEmailSubscribe() != null) {
            user.setSystemEmailSubscribe(settingDTO.getSystemEmailSubscribe());
        }
        if (settingDTO.getServiceEmailSubscribe() != null) {
            user.setServiceEmailSubscribe(settingDTO.getServiceEmailSubscribe());
        }
        userRepository.save(user);
    }

    @Override
    public void sendSystemNotification(SystemNotificationSendDTO sendDTO, Long operatorId) {
        // 1. 创建并保存系统通知实体
        SystemNotification notification = new SystemNotification();
        notification.setTitle(sendDTO.getTitle());
        notification.setContent(sendDTO.getContent());
        notification.setSendChannel(String.join(",", sendDTO.getSendChannel()));
        notification.setCreatedBy(operatorId);
        systemNotificationRepository.save(notification);

        // 2. 如果需要邮件通知，则将一个包含完整信息的“胖消息”推送到Kafka
        if (sendDTO.getSendChannel().contains("EMAIL")) {
            try {
                // 创建一个Map来构建消息体
                Map<String, Object> messagePayload = new HashMap<>();
                messagePayload.put("notificationId", notification.getId());
                messagePayload.put("title", notification.getTitle());
                messagePayload.put("content", notification.getContent());
                // 包含目标用户信息，让消费者无需查询
                messagePayload.put("targetUserIds", sendDTO.getTargetUserIds());

                // 将Map序列化为JSON字符串
                String message = objectMapper.writeValueAsString(messagePayload);

                notificationProducer.sendMessage(NotificationProducer.TOPIC_LOW_PRIORITY, message);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize notification message", e);
                throw new RuntimeException("Failed to serialize notification message", e);
            }
        }
    }
}
