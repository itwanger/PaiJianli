package com.yizhaoqi.pairesume.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UserUpdateProfileDTO {

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @URL(message = "头像必须是合法的URL地址")
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;
}
