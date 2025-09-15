package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.UserSystemNotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * UserSystemNotificationRead Repository
 */
@Repository
public interface UserSystemNotificationReadRepository extends JpaRepository<UserSystemNotificationRead, Long> {

    @Query("SELECT r.systemNotificationId FROM UserSystemNotificationRead r WHERE r.userId = :userId")
    Set<Long> findSystemNotificationIdsByUserId(@Param("userId") Long userId);

    List<UserSystemNotificationRead> findByUserIdAndSystemNotificationId(Long userId, Long notificationId);
}
