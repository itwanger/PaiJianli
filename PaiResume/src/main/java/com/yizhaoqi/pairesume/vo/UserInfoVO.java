package com.yizhaoqi.pairesume.vo;

import com.yizhaoqi.pairesume.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoVO {

    private Long id;
    private String email;
    private String nickname;
    private String avatar;
    private User.Role role;
    private User.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
