package com.taskly.task.infrastructure.messaging;

import com.taskly.task.config.TasklyProperties;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaErrorHandlingConfig {

	@Bean
	DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template, TasklyProperties properties) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
				(record, error) -> new TopicPartition(record.topic() + KafkaTopicsConfig.DEAD_LETTER_SUFFIX, -1));
		TasklyProperties.Retry retry = properties.messaging().retry();
		ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retry.maxRetries());
		backOff.setInitialInterval(retry.initialInterval().toMillis());
		backOff.setMultiplier(retry.multiplier());
		backOff.setMaxInterval(retry.maxInterval().toMillis());
		DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
		handler.addNotRetryableExceptions(InvalidEventException.class);
		return handler;
	}
}
