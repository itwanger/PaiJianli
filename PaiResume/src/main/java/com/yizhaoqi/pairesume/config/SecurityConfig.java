package com.yizhaoqi.pairesume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 的核心配置类
 * <p>
 * 通过 @EnableWebSecurity 注解开启了 Spring Security 的 Web 安全支持。
 * 这个类定义了安全过滤链（SecurityFilterChain）、密码编码器（PasswordEncoder）、
 * 身份验证管理器（AuthenticationManager）等核心组件，它们共同构成了应用的认证和授权策略。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤链 Bean，定义了所有 HTTP 请求的安全策略。
     * <p>
     * 这是 Spring Security 配置的核心，通过链式调用来配置认证和授权规则。
     *
     * @param http                  HttpSecurity 对象，用于构建安全配置。
     * @param authenticationProvider 之前定义的身份验证提供者。
     * @param jwtAuthFilter         自定义的 JWT 认证过滤器。
     * @return 构建好的 SecurityFilterChain 实例。
     * @throws Exception 如果配置过程中发生错误。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider,
                                                   JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                // 1. 禁用 CSRF（跨站请求伪造）保护。
                // 由于我们使用 JWT 进行认证，每个请求都是无状态的，CSRF 保护变得非必需。
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 配置会话管理策略为无状态（STATELESS）。
                // 这意味着服务器不会创建或维护任何 HttpSession，符合 RESTful API 的设计原则。
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 配置 HTTP 请求的授权规则。
                .authorizeHttpRequests(auth -> auth
                        // 允许匿名访问的公共路径
                        .requestMatchers(
                                "/auth/**",
                                "/admin/login",
                                "/pay/callback"
                        ).permitAll()
                        // 需要 ADMIN 权限的路径
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 4. 设置身份验证提供者。
                // 当需要进行身份验证时，Spring Security 会使用这个 Provider。
                .authenticationProvider(authenticationProvider)

                // 5. 添加自定义的 JWT 认证过滤器。
                // 这个过滤器会在 UsernamePasswordAuthenticationFilter 之前执行，
                // 用于解析 JWT、验证签名，并将用户信息设置到 SecurityContext 中。
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
