package com.yizhaoqi.pairesume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenDTO {

    @NotBlank(message = "Refresh Token 不能为空")
    private String refreshToken;
}
