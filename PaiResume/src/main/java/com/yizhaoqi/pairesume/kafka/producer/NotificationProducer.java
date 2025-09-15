package com.yizhaoqi.pairesume.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public static final String TOPIC_HIGH_PRIORITY = "high-priority-notifications";
    public static final String TOPIC_LOW_PRIORITY = "low-priority-notifications";

    /**
     * 发送消息到指定的Topic
     *
     * @param topic   Topic名称
     * @param message 消息内容 (通常是JSON字符串)
     */
    public void sendMessage(String topic, String message) {
        log.info("Sending message to topic {}: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }
}
