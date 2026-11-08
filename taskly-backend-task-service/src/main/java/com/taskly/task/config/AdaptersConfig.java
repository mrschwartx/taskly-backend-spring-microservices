package com.taskly.task.config;

import com.taskly.task.auth.JwtTokenVerifier;
import com.taskly.task.auth.TokenVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdaptersConfig {

	@Bean
	TokenVerifier tokenVerifier(TasklyProperties properties) {
		return new JwtTokenVerifier(properties.auth().secret());
	}
}
