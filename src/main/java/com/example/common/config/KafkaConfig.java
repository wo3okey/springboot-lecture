package com.example.common.config;

import com.example.domain.event.MovieEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 설정 클래스
 *
 * [주요 설정]
 * - ConsumerFactory: Consumer 인스턴스 생성 팩토리
 * - KafkaListenerContainerFactory: @KafkaListener가 사용할 컨테이너 팩토리
 *
 * [핵심 개념]
 * - JsonDeserializer: JSON → Java 객체 역직렬화
 * - trusted.packages: 역직렬화 허용 패키지 (보안상 제한 필요)
 */
@Configuration
public class KafkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Consumer 설정
     */
    @Bean
    public ConsumerFactory<String, MovieEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // 기본 설정
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // 오프셋 설정
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 무중단 리밸런싱 전략 (Kafka 2.4+)
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                CooperativeStickyAssignor.class.getName());

        // Auto Commit 비활성화 (수동 커밋 사용)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 역직렬화 설정
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // JSON 역직렬화 허용 패키지 (보안)
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.*");

        // 타입 매핑 (Producer가 보낸 타입 정보 사용하지 않고 명시적 지정)
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, MovieEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * KafkaListener 컨테이너 팩토리
     *
     * [DLT (Dead Letter Topic) 설정]
     * - 메시지 처리 실패 시 3회 재시도 (1초 간격)
     * - 재시도 후에도 실패하면 DLT로 전송 (원본토픽명.DLT)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MovieEvent> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, MovieEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // 동시 처리 스레드 수 (파티션 수와 맞추면 최적)
        factory.setConcurrency(3);

        // 수동 커밋 모드 설정 (MANUAL_IMMEDIATE: acknowledge() 호출 즉시 커밋)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // DLT Error Handler 설정
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));

        return factory;
    }

    /**
     * Dead Letter Topic Error Handler
     *
     * [동작 흐름]
     * 1. 메시지 처리 실패
     * 2. 1초 간격으로 최대 3회 재시도
     * 3. 재시도 후에도 실패 → DLT(Dead Letter Topic)로 전송
     *
     * [DLT 토픽 명명 규칙]
     * - 원본 토픽: movie-events
     * - DLT 토픽: movie-events.DLT
     */
    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // DLT로 실패 메시지 전송
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    logger.error("[DLT] 메시지 처리 실패 → DLT 전송: topic={}, partition={}, offset={}, error={}",
                            record.topic(), record.partition(), record.offset(), exception.getMessage());
                    return new org.apache.kafka.common.TopicPartition(
                            record.topic() + ".DLT", record.partition());
                });

        // 재시도 설정: 1초 간격, 최대 3회
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // 재시도 시 로깅
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            logger.warn("[Kafka Retry] 재시도 {}/3: topic={}, partition={}, offset={}, error={}",
                    deliveryAttempt, record.topic(), record.partition(), record.offset(), ex.getMessage());
        });

        return errorHandler;
    }
}
