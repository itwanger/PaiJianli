package com.yizhaoqi.pairesume.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    /**
     * 配置Kafka Listener Container Factory，为所有 @KafkaListener 添加统一的错误处理机制。
     * @param consumerFactory a {@link org.springframework.kafka.core.ConsumerFactory} object.
     * @return a {@link org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory} object.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        // 设置一个通用的错误处理器
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    /**
     * 创建一个默认的错误处理器，包含重试和死信队列逻辑。
     *
     * @return a {@link org.springframework.kafka.listener.CommonErrorHandler} object.
     */
    @Bean
    public CommonErrorHandler errorHandler() {
        // 1. 创建一个 DeadLetterPublishingRecoverer
        //    作用：当重试耗尽后，将消息发布到死信队列。
        //    我们通过lambda表达式定义了目标死信Topic的名称：原始Topic + ".DLT"
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", -1));

        // 2. 创建一个 DefaultErrorHandler
        //    参数1: recoverer -> 指定了最终的恢复策略（发送到死信队列）
        //    参数2: FixedBackOff -> 指定了重试策略
        //           - 1000L: 每次重试的间隔时间（毫秒）
        //           - 2L: 最大重试次数。加上首次尝试，总共会尝试3次。
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));

        

        return errorHandler;
    }
}
