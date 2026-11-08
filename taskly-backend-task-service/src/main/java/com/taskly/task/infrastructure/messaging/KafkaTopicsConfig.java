package com.taskly.task.infrastructure.messaging;

import com.taskly.task.config.TasklyProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

	static final String DEAD_LETTER_SUFFIX = ".DLT";

	@Bean
	NewTopic userEventsTopic(TasklyProperties properties) {
		return TopicBuilder.name(properties.messaging().userEventsTopic())
				.partitions(properties.messaging().partitions())
				.replicas(properties.messaging().replicas())
				.build();
	}

	@Bean
	NewTopic userEventsDeadLetterTopic(TasklyProperties properties) {
		return TopicBuilder.name(properties.messaging().userEventsTopic() + DEAD_LETTER_SUFFIX)
				.partitions(properties.messaging().partitions())
				.replicas(properties.messaging().replicas())
				.build();
	}
}
