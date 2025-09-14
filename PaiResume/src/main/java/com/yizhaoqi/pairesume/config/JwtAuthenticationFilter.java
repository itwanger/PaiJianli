package com.yizhaoqi.pairesume.config;

import com.yizhaoqi.pairesume.common.constant.JwtConstants;
import com.yizhaoqi.pairesume.common.constant.RedisKeys;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.common.utils.RedisUtil;
import com.yizhaoqi.pairesume.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(JwtConstants.TOKEN_HEADER);
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith(JwtConstants.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(JwtConstants.TOKEN_PREFIX.length());
        try {
            // Extract email from custom claim
            userEmail = jwtUtil.extractAllClaims(jwt).get("email", String.class);
        } catch (ExpiredJwtException e) {
            // TODO: handle expired token exception, maybe return a custom response
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Check if token is in blacklist
            boolean isTokenBlacklisted = redisUtil.hasKey(RedisKeys.BLACKLIST_ACCESS_TOKEN_KEY + jwt);

            // Validate the token and also check if the user ID from the token matches the one in userDetails
            if (jwtUtil.validateToken(jwt, userDetails) && !isTokenBlacklisted) {
                // Additional check for subject (user ID)
                String userIdFromToken = jwtUtil.extractUsername(jwt); // remember, this is now ID
                if(userIdFromToken.equals(String.valueOf(((User) userDetails).getId()))) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
