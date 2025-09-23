package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.dto.EmailSendCodeDTO;
import com.yizhaoqi.pairesume.dto.LoginDTO;
import com.yizhaoqi.pairesume.dto.PasswordForgotDTO;
import com.yizhaoqi.pairesume.dto.PasswordResetDTO;
import com.yizhaoqi.pairesume.dto.RefreshTokenDTO;
import com.yizhaoqi.pairesume.dto.RegisterDTO;
import com.yizhaoqi.pairesume.service.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return R.ok( "注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<Map<String, String>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, String> tokens = authService.login(loginDTO);
        return R.ok(tokens, "Login successful");
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/email/send-code")
    public R<?> sendEmailCode(@Valid @RequestBody EmailSendCodeDTO emailSendCodeDTO) {
        authService.sendEmailVerificationCode(emailSendCodeDTO);
        return R.ok("验证码已发送，请注意查收");
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public R<?> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            authService.logout(accessToken);
        }
        return R.ok("退出成功");
    }

    /**
     * 刷新 Access Token
     */
    @PostMapping("/refresh-token")
    public R<Map<String, String>> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        Map<String, String> tokens = authService.refreshToken(refreshTokenDTO);
        return R.ok(tokens, "Token refreshed successfully");
    }

    /**
     * 申请发送密码重置验证码
     */
    @PostMapping("/password/forgot")
    public R<?> forgotPassword(@Valid @RequestBody PasswordForgotDTO passwordForgotDTO) {
        authService.sendPasswordResetCode(passwordForgotDTO);
        return R.ok("如果该邮箱已注册，一封密码重置邮件将发送到您的邮箱");
    }

    /**
     * 重置密码
     */
    @PostMapping("/password/reset")
    public R<?> resetPassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {
        authService.resetPassword(passwordResetDTO);
        return R.ok("密码重置成功");
    }
}
