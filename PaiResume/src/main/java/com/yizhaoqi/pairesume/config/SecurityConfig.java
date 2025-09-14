package com.yizhaoqi.pairesume.config;

import com.yizhaoqi.pairesume.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    /**
     * 配置密码加密器，使用 BCrypt 算法。
     * Spring Security 会自动使用这个 Bean 来加密和校验密码。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 定义 UserDetailsService，用于从数据库根据用户名（邮箱）加载用户信息。
     * 这是 Spring Security 进行身份认证的基础。
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }

    /**
     * 配置认证提供者 AuthenticationProvider。
     * 它将 UserDetailsService（如何找用户）和 PasswordEncoder（如何比对密码）两者结合在一起。
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 暴露 AuthenticationManager 为 Bean。
     * 这是认证流程的核心执行者，在登录接口中会用到。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置安全过滤链，定义所有HTTP请求的安全策略。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF 防护，因为我们使用 JWT，它天然免疫 CSRF 攻击。
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理为无状态（STATELESS），因为我们不使用 Session。
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求的授权规则。
                .authorizeHttpRequests(auth -> auth
                        // 允许对 /auth/ 下的所有接口（如注册、登录）进行匿名访问。
                        .requestMatchers("/auth/**").permitAll()
                        // 除了上述明确允许的接口外，所有其他请求都必须经过认证。
                        .anyRequest().authenticated()
                )
                // 指定我们自定义的认证提供者。
                .authenticationProvider(authenticationProvider())
                // 在标准的 UsernamePasswordAuthenticationFilter 过滤器之前，添加我们的 JWT 认证过滤器。
                // 这确保了每个需要认证的请求都会先经过 JWT 的校验。
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
