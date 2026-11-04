package com.taskly.backend.users.application;

public record AuthResult(UserDto user, String token) {
}
