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
@ConditionalOnProperty(name = "ai.deepseek.provider", havingValue = "local", matchIfMissing = true)
public class DeepSeekLocalServiceImpl implements IAiOptimizeService {

    private final RestTemplate restTemplate;
    private final DeepSeekProperties deepSeekProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String optimize(String originalContent) {
        // 1. 构建 Prompt
        String prompt = deepSeekProperties.getPrompt().replace("{INPUT_JSON_HERE}", originalContent);

        // 2. 构建请求体 (本地模型通常也遵循 OpenAI 格式)
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekProperties.getLocal().getModel());
        requestBody.put("messages", Collections.singletonList(message));
        // 本地模型通常不需要 stream 和 temperature 等参数，或使用默认值

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 3. 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    deepSeekProperties.getLocal().getUrl(),
                    entity,
                    String.class
            );

            // 4. 解析并校验响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String responseBody = response.getBody();
                String optimizedContent = objectMapper.readTree(responseBody)
                        .path("choices").get(0)
                        .path("message").path("content").asText();

                // 最终校验
                objectMapper.readTree(optimizedContent);
                return optimizedContent;
            }
        } catch (JsonProcessingException e) {
            log.error("AI local response JSON processing error", e);
            return null;
        } catch (Exception e) {
            log.error("Error calling DeepSeek Local API", e);
        }
        return null;
    }
}
