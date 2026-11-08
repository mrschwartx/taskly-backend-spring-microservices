package com.taskly.user.config;

import com.taskly.user.application.AvatarStorage;
import com.taskly.user.domain.PasswordHasher;
import com.taskly.user.domain.TokenService;
import com.taskly.user.infrastructure.BcryptPasswordHasher;
import com.taskly.user.infrastructure.JwtTokenService;
import com.taskly.user.infrastructure.LocalAvatarStorage;
import com.taskly.user.presentation.AuthCookie;
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
