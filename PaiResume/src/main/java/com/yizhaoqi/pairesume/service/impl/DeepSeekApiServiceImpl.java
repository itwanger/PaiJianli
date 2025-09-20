package com.yizhaoqi.pairesume.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yizhaoqi.pairesume.config.DeepSeekProperties;
import com.yizhaoqi.pairesume.service.IAiOptimizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.deepseek.provider", havingValue = "api")
public class DeepSeekApiServiceImpl implements IAiOptimizeService {

    private final RestTemplate restTemplate;
    private final DeepSeekProperties deepSeekProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String optimize(String originalContent) {
        // 1. 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekProperties.getApi().getKey());

        // 2. 构建 Prompt
        String prompt = deepSeekProperties.getPrompt().replace("{INPUT_JSON_HERE}", originalContent);

        // 3. 构建请求体
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekProperties.getApi().getModel());
        requestBody.put("messages", Collections.singletonList(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    deepSeekProperties.getApi().getUrl(),
                    entity,
                    String.class
            );

            // 5. 解析并校验响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String responseBody = response.getBody();
                // 提取 choices -> message -> content
                String optimizedContent = objectMapper.readTree(responseBody)
                        .path("choices").get(0)
                        .path("message").path("content").asText();

                // 最终校验返回的内容是否是合法JSON
                objectMapper.readTree(optimizedContent);
                return optimizedContent;
            }
        } catch (JsonProcessingException e) {
            log.error("AI api response JSON processing error", e);
            // AI 返回的 content 不是一个合法的 JSON
            return null;
        } catch (Exception e) {
            log.error("Error calling DeepSeek API", e);
        }
        return null;
    }
}
