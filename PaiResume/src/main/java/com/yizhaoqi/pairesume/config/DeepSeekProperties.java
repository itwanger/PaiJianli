package com.yizhaoqi.pairesume.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.deepseek")
public class DeepSeekProperties {

    private String provider;
    private String prompt;
    private Api api;
    private Local local;

    @Data
    public static class Api {
        private String url;
        private String key;
        private String model;
    }

    @Data
    public static class Local {
        private String url;
        private String model;
    }
}
