package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.common.utils.RequestUtils;
import com.yizhaoqi.pairesume.dto.LoginDTO;
import com.yizhaoqi.pairesume.dto.SystemNotificationSendDTO;
import com.yizhaoqi.pairesume.service.IAuthService;
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

import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final INotificationService notificationService;
    private final IAuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * 管理员登录
     *
     * @param loginDTO 登录 DTO
     * @return 包含 tokens 的 Map
     */
    @PostMapping("/login")
    public R<Map<String, String>> adminLogin(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, String> tokens = authService.adminLogin(loginDTO);
        return R.ok(tokens, "Admin login successful");
    }

    /**
     * 管理员发送系统通知
     * <p>
     * 需要 ADMIN 权限
     *
     * @param sendDTO 请求体
     * @return 操作结果
     */
    @PostMapping("/notifications")
    @PreAuthorize("hasAuthority('ADMIN')")
    public R<Void> sendSystemNotification(@RequestBody SystemNotificationSendDTO sendDTO, HttpServletRequest request) {
        Optional<Long> userId = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userId.isEmpty()) {
            return R.fail("未授权的访问");
        }
        notificationService.sendSystemNotification(sendDTO, userId.get());
        return R.ok();
    }
}
