package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.dto.NotificationSettingDTO;
import com.yizhaoqi.pairesume.service.INotificationService;
import com.yizhaoqi.pairesume.vo.NotificationSettingVO;
import com.yizhaoqi.pairesume.vo.NotificationVO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public R<Page<NotificationVO>> getNotifications(@PageableDefault(sort = "createdAt,desc") Pageable pageable, HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            Claims claims = jwtUtil.extractAllClaims(accessToken);
            Long userId = Long.parseLong(claims.getSubject());
            Page<NotificationVO> notifications = notificationService.getNotifications(userId, pageable);
            return R.ok(notifications);
        } else {
            return R.fail("未认证用户");
        }
    }

    @PostMapping("/{id}/read")
    public R<Void> markAsRead(@PathVariable("id") Long notificationId, @RequestParam("type") String type, HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            Claims claims = jwtUtil.extractAllClaims(accessToken);
            Long userId = Long.parseLong(claims.getSubject());
            notificationService.markAsRead(userId, notificationId, type);
            return R.ok();
        } else {
            return R.fail("未认证用户");
        }
    }

    @PostMapping("/read-all")
    public R<Void> markAllAsRead(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            Claims claims = jwtUtil.extractAllClaims(accessToken);
            Long userId = Long.parseLong(claims.getSubject());
            notificationService.markAllAsRead(userId);
            return R.ok();
        } else {
            return R.fail("未认证用户");
        }
    }

    @GetMapping("/settings")
    public R<NotificationSettingVO> getNotificationSettings(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            Claims claims = jwtUtil.extractAllClaims(accessToken);
            Long userId = Long.parseLong(claims.getSubject());
            NotificationSettingVO settings = notificationService.getNotificationSettings(userId);
            return R.ok(settings);
        } else {
            return R.fail("未认证用户");
        }
    }

    @PostMapping("/settings")
    public R<Void> updateNotificationSettings(@RequestBody NotificationSettingDTO settingDTO, HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            Claims claims = jwtUtil.extractAllClaims(accessToken);
            Long userId = Long.parseLong(claims.getSubject());
            notificationService.updateNotificationSettings(userId, settingDTO);
            return R.ok();
        } else {
            return R.fail("未认证用户");
        }
    }
}

