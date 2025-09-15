package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户可选值
     */
    Optional<User> findByEmail(String email);

    /**
     * 判断邮箱是否存在
     *
     * @param email 邮箱
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByEmail(String email);

    /**
     * 以流式方式查询所有订阅了系统邮件的用户，避免OOM。
     * 注意：调用此方法的服务层方法必须在一个只读事务中 (@Transactional(readOnly = true))。
     * @param subscribed 订阅状态
     * @return 用户流
     */
    Stream<User> findBySystemEmailSubscribe(boolean subscribed);
}
