package com.taskly.backend.shared.infrastructure.http;

public record ApiError(int status, String message) {
}
