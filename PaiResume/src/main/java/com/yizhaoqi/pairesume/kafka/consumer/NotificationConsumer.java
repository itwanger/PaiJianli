package com.yizhaoqi.pairesume.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yizhaoqi.pairesume.entity.SystemNotification;
import com.yizhaoqi.pairesume.entity.User;
import com.yizhaoqi.pairesume.repository.SystemNotificationRepository;
import com.yizhaoqi.pairesume.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final SystemNotificationRepository systemNotificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    /**
     * 监听低优先级通知Topic。
     * 异常处理：
     * - 当此方法抛出任何异常时，由 KafkaConfig 中配置的 DefaultErrorHandler 进行处理。
     * - DefaultErrorHandler 会进行2次重试（间隔1秒）。
     * - 如果3次尝试（1次原始 + 2次重试）全部失败，消息将被发送到 "low-priority-notifications.DLT" 死信队列。
     *
     * @param message 消息内容
     * @throws IOException 如果JSON解析失败
     */
    @KafkaListener(topics = "low-priority-notifications",
                   groupId = "notification-group",
                   containerFactory = "kafkaListenerContainerFactory")
    @Transactional(readOnly = true)
    public void handleLowPriorityNotifications(String message) throws IOException {
        log.info("Received low-priority message: {}", message);
        try {
            // 1. 解析"胖消息"
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            String title = (String) payload.get("title");
            String content = (String) payload.get("content");
            List<Integer> targetUserIdsInt = (List<Integer>) payload.get("targetUserIds");
            List<Long> targetUserIds = targetUserIdsInt == null ? null : targetUserIdsInt.stream()
                                                                            .map(Integer::longValue)
                                                                            .collect(Collectors.toList());

            // 2. 根据 targetUserIds 决定目标用户
            List<User> targetUsers;
            if (targetUserIds == null || targetUserIds.isEmpty()) {
                // 如果目标ID列表为空，则流式处理所有订阅了系统邮件的用户，避免OOM
                log.info("Target users are all subscribed users. Processing in stream mode.");
                try (Stream<User> userStream = userRepository.findBySystemEmailSubscribe(true)) {
                    userStream.forEach(user -> {
                        // 确保用户也订阅了该渠道
                        if (user.getSystemEmailSubscribe()) {
                            SimpleMailMessage mailMessage = new SimpleMailMessage();
                            mailMessage.setFrom(emailFrom);
                            mailMessage.setTo(user.getEmail());
                            mailMessage.setSubject("【派简历】系统通知: " + title);
                            mailMessage.setText(content);
                            mailSender.send(mailMessage);
                        }
                    });
                }
                // 流式处理已完成，直接返回
                return;

            } else {
                // 如果有具体的目标ID，则只查询这些用户
                log.info("Target users are specified with IDs: {}", targetUserIds);
                targetUsers = userRepository.findAllById(targetUserIds);
            }

            log.info("Preparing to send notification '{}' to {} users.", title, targetUsers.size());

            // 3. 遍历并发送邮件 (仅针对指定ID列表的情况)
            for (User user : targetUsers) {
                // 确保用户也订阅了该渠道
                if (user.getSystemEmailSubscribe()) {
                    SimpleMailMessage mailMessage = new SimpleMailMessage();
                    mailMessage.setFrom(emailFrom);
                    mailMessage.setTo(user.getEmail());
                    mailMessage.setSubject("【派简历】系统通知: " + title);
                    mailMessage.setText(content);
                    mailSender.send(mailMessage);
                }
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to parse notification message: {}", message, e);
            throw e;
        } catch (Exception e) {
            log.error("An error occurred while processing message, will retry: {}", message, e);
            throw e;
        }
    }

    /**
     * 监听高优先级通知Topic。
     * 同样应用了 KafkaConfig 中定义的重试和死信队列策略。
     *
     * @param message 消息内容
     */
    @KafkaListener(topics = "high-priority-notifications",
                   groupId = "notification-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleHighPriorityNotifications(String message) {
        log.info("Received high-priority message: {}", message);
        // TODO: 实现高优先级通知的处理逻辑
        // 1. 解析消息，通常会包含 userId 和 notificationId/content
        // 2. 查询用户信息，获取邮箱
        // 3. 调用邮件服务发送邮件
        // 4. 如果发生异常，同样会自动重试并进入死信队列 "high-priority-notifications.DLT"
    }
}

