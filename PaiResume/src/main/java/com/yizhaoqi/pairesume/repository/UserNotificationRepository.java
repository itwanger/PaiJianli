package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserNotification Repository
 */
@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Page<UserNotification> findByUserId(Long userId, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE UserNotification un SET un.status = 'READ' WHERE un.userId = :userId AND un.status = 'UNREAD'")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
