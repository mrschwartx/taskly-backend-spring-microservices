package com.taskly.user.shared.infrastructure.http;

public record ApiError(int status, String message) {
}
