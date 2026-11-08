package com.taskly.user.application;

public record AuthResult(UserDto user, String token) {
}
