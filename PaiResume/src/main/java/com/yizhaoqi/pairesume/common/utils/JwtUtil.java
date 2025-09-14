package com.yizhaoqi.pairesume.common.utils;

import com.yizhaoqi.pairesume.common.constant.JwtConstants;
import com.yizhaoqi.pairesume.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成包含 Access Token, Refresh Token 和 JTI 的 Token Map
     */
    public Map<String, String> generateTokens(UserDetails userDetails) {
        String jti = UUID.randomUUID().toString();
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.CLAIM_TYPE, userDetails.getAuthorities().iterator().next().getAuthority());

        String accessToken = createToken(claims, userDetails.getUsername(), accessTokenExpirationMs, jti);
        String refreshToken = createToken(new HashMap<>(), userDetails.getUsername(), refreshTokenExpirationMs, null);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        return tokens;
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.CLAIM_TYPE, user.getRole().name());
        claims.put("email", user.getEmail());
        return createToken(claims, String.valueOf(user.getId()), accessTokenExpirationMs, UUID.randomUUID().toString());
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(User user) {
        return createToken(new HashMap<>(), String.valueOf(user.getId()), refreshTokenExpirationMs, null);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs, String jti) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(jti) // 设置 JTI
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 Token 中解析 Claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中解析 JTI
     */
    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    /**
     * 从 Token 中解析用户名 (subject)
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * 判断 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        // The detailed validation (username matching) is now handled in the filter.
        // Here, we just check for expiration.
        return !isTokenExpired(token);
    }
}
