package com.yizhaoqi.pairesume.service;

import com.yizhaoqi.pairesume.dto.EmailSendCodeDTO;
import com.yizhaoqi.pairesume.dto.LoginDTO;
import com.yizhaoqi.pairesume.dto.PasswordForgotDTO;
import com.yizhaoqi.pairesume.dto.PasswordResetDTO;
import com.yizhaoqi.pairesume.dto.RefreshTokenDTO;
import com.yizhaoqi.pairesume.dto.RegisterDTO;

import java.util.Map;

public interface IAuthService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 包含 access_token 和 refresh_token 的 Map
     */
    Map<String, String> login(LoginDTO loginDTO);

    /**
     * 管理员登录
     *
     * @param loginDTO 登录信息
     * @return 包含 access_token 和 refresh_token 的 Map
     */
    Map<String, String> adminLogin(LoginDTO loginDTO);

    /**
     * 发送邮箱验证码
     *
     * @param emailSendCodeDTO 包含邮箱信息的 DTO
     */
    void sendEmailVerificationCode(EmailSendCodeDTO emailSendCodeDTO);

    /**
     * 用户退出登录
     *
     * @param accessToken 当前用户的 Access Token
     */
    void logout(String accessToken);

    /**
     * 刷新 Access Token
     *
     * @param refreshTokenDTO 包含 Refresh Token 的 DTO
     * @return 包含新 Access Token 的 Map
     */
    Map<String, String> refreshToken(RefreshTokenDTO refreshTokenDTO);

    /**
     * 发送密码重置邮件
     *
     * @param passwordForgotDTO 包含邮箱信息的 DTO
     */
    void sendPasswordResetCode(PasswordForgotDTO passwordForgotDTO);

    /**
     * 重置密码
     *
     * @param passwordResetDTO 包含邮箱、验证码和新密码的 DTO
     */
    void resetPassword(PasswordResetDTO passwordResetDTO);
}
