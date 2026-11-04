package com.taskly.backend.shared.infrastructure.http;

import com.taskly.backend.config.TasklyProperties;
import com.taskly.backend.shared.domain.DomainException;
import jakarta.servlet.ServletException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private static final String DEFAULT_MESSAGE = "We're having technical issues. Please try again later.";

	private static final Map<String, HttpStatus> STATUS_BY_CODE = Map.of(
			"VALIDATION_ERROR", HttpStatus.BAD_REQUEST,
			"NOT_FOUND", HttpStatus.NOT_FOUND,
			"ALREADY_EXISTS", HttpStatus.UNPROCESSABLE_CONTENT,
			"AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED,
			"FORBIDDEN", HttpStatus.FORBIDDEN);

	private final long avatarMaxMegabytes;

	public GlobalExceptionHandler(TasklyProperties properties) {
		this.avatarMaxMegabytes = properties.storage().avatarMaxSize().toMegabytes();
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ApiError> handleDomain(DomainException error) {
		HttpStatus status = STATUS_BY_CODE.getOrDefault(error.getCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		return reject(status.value(), error.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException error) {
		return reject(HttpStatus.BAD_REQUEST.value(), "Malformed JSON body");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiError> handleUploadTooLarge(MaxUploadSizeExceededException error) {
		return reject(HttpStatus.BAD_REQUEST.value(), "File size exceeds " + avatarMaxMegabytes + "MB");
	}

	@ExceptionHandler(ServletException.class)
	public ResponseEntity<?> handleServlet(ServletException error) {
		if (error instanceof ErrorResponse errorResponse) {
			int status = errorResponse.getStatusCode().value();
			if (status == HttpStatus.NOT_FOUND.value()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
			}
			if (errorResponse.getStatusCode().is4xxClientError()) {
				return reject(status, errorResponse.getBody().getDetail());
			}
		}
		return handleUnexpected(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception error) {
		log.error("Unhandled error: {}", error.getMessage(), error);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), DEFAULT_MESSAGE));
	}

	private ResponseEntity<ApiError> reject(int status, String message) {
		log.warn("Request rejected status={} message={}", status, message);
		return ResponseEntity.status(status).body(new ApiError(status, message));
	}
}
