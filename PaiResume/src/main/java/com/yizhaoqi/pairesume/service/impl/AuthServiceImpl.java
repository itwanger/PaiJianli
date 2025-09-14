package com.yizhaoqi.pairesume.service.impl;

import com.yizhaoqi.pairesume.common.constant.RedisKeys;
import com.yizhaoqi.pairesume.common.exception.ServiceException;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.common.utils.RedisUtil;
import com.yizhaoqi.pairesume.dto.EmailSendCodeDTO;
import com.yizhaoqi.pairesume.dto.EmailVerifyDTO;
import com.yizhaoqi.pairesume.dto.LoginDTO;
import com.yizhaoqi.pairesume.dto.PasswordForgotDTO;
import com.yizhaoqi.pairesume.dto.PasswordResetDTO;
import com.yizhaoqi.pairesume.dto.RefreshTokenDTO;
import com.yizhaoqi.pairesume.dto.RegisterDTO;
import com.yizhaoqi.pairesume.entity.User;
import com.yizhaoqi.pairesume.repository.UserRepository;
import com.yizhaoqi.pairesume.service.IAuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    public void register(RegisterDTO registerDTO) {
        // 1. 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new ServiceException("邮箱已注册", 1001);
        }

        // 2. 创建用户实体
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        // 密码加密
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        // 默认角色和状态在User实体中已设置

        // 3. 保存到数据库
        userRepository.save(user);

        // 4. 发送激活邮件
        sendEmailVerificationCode(new EmailSendCodeDTO(user.getEmail()));
    }

    @Override
    public Map<String, String> login(LoginDTO loginDTO) {
        // 1. 使用 AuthenticationManager 进行用户认证
        // 这一步会自动调用 UserDetailsService 加载用户信息，并比对密码
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        // 2. 认证通过后，从数据库获取用户信息
        // 此时可以确定用户是存在的，且密码正确
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new ServiceException("用户信息加载失败", 500)); // 理论上认证通过后不会发生

        // 3. 检查用户状态 (AuthenticationManager 默认已检查 LOCKED 和 INACTIVE 状态，这里可以作为双重保险)
        if (user.getStatus() == User.Status.PENDING) {
            throw new ServiceException("邮箱未激活", 2002);
        }

        // 4. 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        String jti = jwtUtil.extractJti(accessToken);

        // 5. 存储 Refresh Token 到 Redis
        String redisKey = RedisKeys.REFRESH_TOKEN_KEY + "user:" + user.getId();
        Map<String, Object> refreshTokenData = new HashMap<>();
        refreshTokenData.put("token", refreshToken);
        refreshTokenData.put("jti", jti);
        redisUtil.set(redisKey, refreshTokenData, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);

        // 6. 更新用户最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 7. 返回 Tokens
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        return tokens;
    }

    @Override
    public void sendEmailVerificationCode(EmailSendCodeDTO emailSendCodeDTO) {
        String email = emailSendCodeDTO.getEmail();
        // 1. 校验用户是否存在且状态为 PENDING
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException("用户不存在")); // 出于安全，不明确提示用户是否存在

        if (user.getStatus() != User.Status.PENDING) {
            throw new ServiceException("账户状态异常，无需重复激活");
        }

        // 2. TODO: 增加防刷校验，例如检查60秒内是否重复发送

        // 3. 生成6位随机数字验证码
        String code = String.format("%06d", new SecureRandom().nextInt(999999));

        // 4. 将验证码存入 Redis，有效期5分钟
        redisUtil.set(RedisKeys.EMAIL_REGISTER_CODE_KEY + email, code, 5, TimeUnit.MINUTES);

        // 5. 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your_email@example.com"); // TODO: 从配置文件读取
        message.setTo(email);
        message.setSubject("【派简历】邮箱激活验证码");
        message.setText("您的激活验证码是：" + code + "，有效期为5分钟。");
        mailSender.send(message);
    }

    @Override
    public void verifyEmailCode(EmailVerifyDTO emailVerifyDTO) {
        String email = emailVerifyDTO.getEmail();
        String code = emailVerifyDTO.getCode();

        // 1. 从 Redis 中获取验证码
        String redisCode = redisUtil.get(RedisKeys.EMAIL_REGISTER_CODE_KEY + email);

        // 2. 校验验证码
        if (redisCode == null) {
            throw new ServiceException("验证码已过期", 3003);
        }
        if (!redisCode.equals(code)) {
            // TODO: 增加错误次数过多校验
            throw new ServiceException("验证码错误", 3002);
        }

        // 3. 更新用户状态为 ACTIVE
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException("用户不存在")); // 理论上此时用户一定存在
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        // 4. 从 Redis 中删除验证码
        redisUtil.delete(RedisKeys.EMAIL_REGISTER_CODE_KEY + email);
    }

    @Override
    public void logout(String accessToken) {
        // 1. 解析 access_token
        Claims claims = jwtUtil.extractAllClaims(accessToken);
        Long userId = Long.parseLong(claims.getSubject());
        Date expiration = claims.getExpiration();
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();

        // 2. 如果 token 未过期，则将其加入黑名单
        if (remainingMillis > 0) {
            redisUtil.set(
                    RedisKeys.BLACKLIST_ACCESS_TOKEN_KEY + accessToken,
                    "blacklisted",
                    remainingMillis,
                    TimeUnit.MILLISECONDS
            );
        }

        // 3. 从 Redis 中删除对应的 Refresh Token
        String redisKey = RedisKeys.REFRESH_TOKEN_KEY + "user:" + userId;
        redisUtil.delete(redisKey);
    }

    @Override
    public Map<String, String> refreshToken(RefreshTokenDTO refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.getRefreshToken();
        
        // 1. 从 refresh token 解析用户 ID
        Long userId = Long.parseLong(jwtUtil.extractUsername(refreshToken));

        // 2. 从 Redis 验证 refresh token 的有效性
        String redisKey = RedisKeys.REFRESH_TOKEN_KEY + "user:" + userId;
        Map<String, Object> refreshTokenData = redisUtil.get(redisKey);

        if (refreshTokenData == null || !refreshToken.equals(refreshTokenData.get("token"))) {
            throw new ServiceException("Refresh Token 无效", 6001);
        }

        // 3. 检查 refresh token 是否过期
        if (jwtUtil.isTokenExpired(refreshToken)) {
             redisUtil.delete(redisKey); // 过期就清理掉
            throw new ServiceException("Refresh Token 已过期", 6002);
        }

        // 4. 加载用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("用户不存在", 404));

        // 5. 生成新的 Access Token
        String newAccessToken = jwtUtil.generateAccessToken(user);
        
        // --- Refresh Token Rotation (可选但推荐) ---
        // 为了更高的安全性，我们也可以在这里生成一个新的 Refresh Token
        // String newRefreshToken = jwtUtil.generateRefreshToken(user);
        // String newJti = jwtUtil.extractJti(newAccessToken);
        // refreshTokenData.put("token", newRefreshToken);
        // refreshTokenData.put("jti", newJti);
        // redisUtil.set(redisKey, refreshTokenData, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);
        // -----------------------------------------

        // 6. 返回新的 Access Token
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", newAccessToken);
        // 如果实现了 Rotation，也返回新的 refresh_token
        // tokens.put("refresh_token", newRefreshToken); 
        return tokens;
    }

    @Override
    public void sendPasswordResetCode(PasswordForgotDTO passwordForgotDTO) {
        String email = passwordForgotDTO.getEmail();
        // 1. 无论邮箱是否存在，都执行后续流程，防止用户枚举攻击
        userRepository.findByEmail(email).ifPresent(user -> {
            // 2. TODO: 增加防刷校验

            // 3. 生成6位随机数字验证码
            String code = String.format("%06d", new SecureRandom().nextInt(999999));

            // 4. 将验证码存入 Redis，有效期5分钟
            redisUtil.set(RedisKeys.EMAIL_RESET_PASSWORD_CODE_KEY + email, code, 5, TimeUnit.MINUTES);

            // 5. 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your_email@example.com"); // TODO: 从配置文件读取
            message.setTo(email);
            message.setSubject("【派简历】密码重置验证码");
            message.setText("您的密码重置验证码是：" + code + "，有效期为5分钟。");
            mailSender.send(message);
        });
    }

    @Override
    public void resetPassword(PasswordResetDTO passwordResetDTO) {
        String email = passwordResetDTO.getEmail();
        String code = passwordResetDTO.getCode();
        String newPassword = passwordResetDTO.getNewPassword();

        // 1. 从 Redis 中获取验证码
        String redisCode = redisUtil.get(RedisKeys.EMAIL_RESET_PASSWORD_CODE_KEY + email);

        // 2. 校验验证码
        if (redisCode == null) {
            throw new ServiceException("验证码已过期", 4002);
        }
        if (!redisCode.equals(code)) {
            throw new ServiceException("验证码错误", 4001);
        }

        // 3. 更新用户密码
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException("用户不存在")); // 理论上此时用户一定存在
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. 从 Redis 中删除验证码
        redisUtil.delete(RedisKeys.EMAIL_RESET_PASSWORD_CODE_KEY + email);
    }
}
