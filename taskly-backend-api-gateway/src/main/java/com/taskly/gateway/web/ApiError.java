package com.taskly.gateway.web;

public record ApiError(int status, String message) {
}
