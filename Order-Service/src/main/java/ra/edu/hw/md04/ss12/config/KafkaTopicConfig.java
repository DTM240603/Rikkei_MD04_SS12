package ra.edu.hw.md04.ss12.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
