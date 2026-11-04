package com.taskly.backend.users.application;

public record AvatarUpload(String contentType, byte[] content) {
}
