package com.taskly.backend.config;

import com.taskly.backend.users.application.AvatarStorage;
import com.taskly.backend.users.domain.PasswordHasher;
import com.taskly.backend.users.domain.TokenService;
import com.taskly.backend.users.infrastructure.BcryptPasswordHasher;
import com.taskly.backend.users.infrastructure.JwtTokenService;
import com.taskly.backend.users.infrastructure.LocalAvatarStorage;
import com.taskly.backend.users.presentation.AuthCookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdaptersConfig {

	@Bean
	PasswordHasher passwordHasher(TasklyProperties properties) {
		return new BcryptPasswordHasher(properties.auth().bcryptRounds());
	}

	@Bean
	TokenService tokenService(TasklyProperties properties) {
		return new JwtTokenService(properties.auth().secret(), properties.auth().tokenTtl());
	}

	@Bean
	AvatarStorage avatarStorage(TasklyProperties properties) {
		return new LocalAvatarStorage(properties.storage().publicPath());
	}

	@Bean
	AuthCookie authCookie(TasklyProperties properties) {
		return new AuthCookie(properties.auth().cookieName(), properties.auth().tokenTtl(),
				properties.auth().cookieDomain(), properties.app().production());
	}
}
