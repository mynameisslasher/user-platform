package org.example.userdbapi.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic userAccountTopic(
            @Value("${app.kafka.user-topic:user.account}") String topic){
        log.info("Kafka topic config initialized, ensuring topic exists: {}", topic);
        return new NewTopic(topic, 1, (short) 1);
    }
}
