package com.yizhaoqi.pairesume.common.utils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

/**
 * 请求处理相关工具类
 */
public class RequestUtils {

    /**
     * 从 HttpServletRequest 的 Authorization Header 中提取用户ID
     * @param request 请求对象
     * @param jwtUtil JWT工具类实例
     * @return 包含用户ID的Optional，如果Token无效或不存在则为空
     */
    public static Optional<Long> getUserIdFromRequest(HttpServletRequest request, JwtUtil jwtUtil) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String accessToken = authHeader.substring(7);
                Claims claims = jwtUtil.extractAllClaims(accessToken);
                return Optional.of(Long.parseLong(claims.getSubject()));
            } catch (Exception e) {
                // Token 解析失败或过期
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
