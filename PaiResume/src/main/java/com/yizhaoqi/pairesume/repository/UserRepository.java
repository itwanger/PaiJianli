package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}
