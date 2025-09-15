package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.dto.SystemNotificationSendDTO;
import com.yizhaoqi.pairesume.service.INotificationService;
import com.yizhaoqi.pairesume.vo.NotificationVO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminController {

    private final INotificationService notificationService;

    private final JwtUtil jwtUtil;

    /**
     * 管理员发送系统通知
     * <p>
     * 需要 ADMIN 权限
     *
     * @param sendDTO 请求体
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public R<Void> sendSystemNotification(@RequestBody SystemNotificationSendDTO sendDTO, HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            Claims claims = jwtUtil.extractAllClaims(accessToken);
            Long userId = Long.parseLong(claims.getSubject());
            notificationService.sendSystemNotification(sendDTO, userId);
            return R.ok();
        } else {
            return R.fail("未授权的访问");
        }
    }
}
