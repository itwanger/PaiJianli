package com.yizhaoqi.pairesume.config;

import com.yizhaoqi.pairesume.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        // 返回一个 lambda 表达式实现的 UserDetailsService 接口
        // 当 Spring Security 需要验证用户时，会调用这个 lambda 表达式
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }
}
