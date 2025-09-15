package com.yizhaoqi.pairesume.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * 用户系统通知已读状态表
 */
@Data
@Entity
@Table(name = "user_system_notification_read",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "systemNotificationId"}))
public class UserSystemNotificationRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long systemNotificationId;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp readAt;
}
