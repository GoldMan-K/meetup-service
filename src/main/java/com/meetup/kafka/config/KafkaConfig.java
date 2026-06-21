package com.meetup.kafka.config;

import com.meetup.kafka.event.MemberSuspendedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);
    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // ── Producer ──────────────────────────────────────────────────────────────

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ── Consumer: member.suspended ────────────────────────────────────────────

    @Bean
    public DefaultKafkaConsumerFactory<String, MemberSuspendedEvent> memberSuspendedConsumerFactory() {
        // member-service 는 ADD_TYPE_INFO_HEADERS=false 로 발행하므로
        // JsonDeserializer 생성자 두 번째 인자 false = "타입 헤더 무시하고 지정 클래스로 역직렬화"
        JsonDeserializer<MemberSuspendedEvent> jsonDeserializer =
                new JsonDeserializer<>(MemberSuspendedEvent.class, false);
        jsonDeserializer.addTrustedPackages("*");

        ErrorHandlingDeserializer<MemberSuspendedEvent> valueDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);
        ErrorHandlingDeserializer<String> keyDeserializer =
                new ErrorHandlingDeserializer<>(new StringDeserializer());

        Map<String, Object> config = buildConsumerConfig();
        return new DefaultKafkaConsumerFactory<>(config, keyDeserializer, valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MemberSuspendedEvent> memberSuspendedListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MemberSuspendedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(memberSuspendedConsumerFactory());
        factory.setCommonErrorHandler(commonErrorHandler());
        return factory;
    }

    // ── 공통 ──────────────────────────────────────────────────────────────────

    private Map<String, Object> buildConsumerConfig() {
        Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // JsonDeserializer 인스턴스를 직접 주입하므로 yml의 spring.json.* 설정은 제거
        config.remove(JsonDeserializer.TRUSTED_PACKAGES);
        config.remove(JsonDeserializer.TYPE_MAPPINGS);
        config.remove(JsonDeserializer.KEY_DEFAULT_TYPE);
        config.remove(JsonDeserializer.VALUE_DEFAULT_TYPE);
        config.remove(JsonDeserializer.USE_TYPE_INFO_HEADERS);
        config.remove(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS);
        return config;
    }

    @Bean
    public CommonErrorHandler commonErrorHandler() {
        return new DefaultErrorHandler((record, ex) ->
                log.error("[Kafka] 메시지 처리 실패 및 건너뜀 - topic={}, partition={}, offset={}, error={}",
                        record.topic(), record.partition(), record.offset(), ex.getMessage(), ex)
        );
    }
}


