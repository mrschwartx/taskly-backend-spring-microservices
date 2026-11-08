package com.taskly.user.infrastructure.messaging;

import com.taskly.user.config.TasklyProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

	@Bean
	NewTopic userEventsTopic(TasklyProperties properties) {
		return TopicBuilder.name(properties.messaging().userEventsTopic())
				.partitions(properties.messaging().partitions())
				.replicas(properties.messaging().replicas())
				.build();
	}
}
