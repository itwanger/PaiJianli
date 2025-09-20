package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.common.utils.RequestUtils;
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

import java.util.Optional;

@RestController
@RequestMapping("/user/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public R<Page<NotificationVO>> getNotifications(@PageableDefault(sort = "createdAt,desc") Pageable pageable, HttpServletRequest request) {
        Optional<Long> userId = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userId.isEmpty()) {
            return R.fail("未认证用户");
        } else {
            Page<NotificationVO> notifications = notificationService.getNotifications(userId.get(), pageable);
            return R.ok(notifications);
        }
    }

    @PostMapping("/{id}/read")
    public R<Void> markAsRead(@PathVariable("id") Long notificationId, @RequestParam("type") String type, HttpServletRequest request) {
        Optional<Long> userId = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userId.isEmpty()) {
            return R.fail("未认证用户");
        } else {
            notificationService.markAsRead(userId.get(), notificationId, type);
            return R.ok();
        }
    }

    @PostMapping("/read-all")
    public R<Void> markAllAsRead(HttpServletRequest request) {
        Optional<Long> userId = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userId.isEmpty()) {
            return R.fail("未认证用户");
        } else {
            notificationService.markAllAsRead(userId.get());
            return R.ok();
        }
    }

    @GetMapping("/settings")
    public R<NotificationSettingVO> getNotificationSettings(HttpServletRequest request) {
        Optional<Long> userId = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userId.isEmpty()) {
            return R.fail("未认证用户");
        } else {
            NotificationSettingVO settings = notificationService.getNotificationSettings(userId.get());
            return R.ok(settings);
        }
    }

    @PostMapping("/settings")
    public R<Void> updateNotificationSettings(@RequestBody NotificationSettingDTO settingDTO, HttpServletRequest request) {
        Optional<Long> userId = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userId.isEmpty()) {
            return R.fail("未认证用户");
        } else {
            notificationService.updateNotificationSettings(userId.get(), settingDTO);
            return R.ok();
        }
    }
}

