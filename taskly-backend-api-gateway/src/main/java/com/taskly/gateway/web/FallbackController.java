package com.taskly.gateway.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

	static final String UNAVAILABLE_MESSAGE = "Service temporarily unavailable. Please try again later.";

	private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

	@RequestMapping("/fallback/{service}")
	public ResponseEntity<ApiError> fallback(@PathVariable String service) {
		log.warn("Fallback served for {}", service);
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.header(HttpHeaders.RETRY_AFTER, "5")
				.body(new ApiError(HttpStatus.SERVICE_UNAVAILABLE.value(), UNAVAILABLE_MESSAGE));
	}
}
