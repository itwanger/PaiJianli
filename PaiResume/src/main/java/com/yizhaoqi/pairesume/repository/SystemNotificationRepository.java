package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SystemNotification Repository
 */
@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
}
