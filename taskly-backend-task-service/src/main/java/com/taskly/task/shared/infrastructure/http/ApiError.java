package com.taskly.task.shared.infrastructure.http;

public record ApiError(int status, String message) {
}
