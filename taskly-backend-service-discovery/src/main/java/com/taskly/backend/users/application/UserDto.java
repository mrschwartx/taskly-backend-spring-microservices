package com.taskly.backend.users.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taskly.backend.shared.domain.Timestamps;
import com.taskly.backend.users.domain.User;

public record UserDto(
		@JsonProperty("_id") String id,
		String username,
		String email,
		String avatar,
		String createdAt,
		String updatedAt) {

	public static UserDto from(User user) {
		return new UserDto(
				user.getId().toString(),
				user.getUsername().value(),
				user.getEmail().value(),
				user.getAvatar(),
				Timestamps.format(user.getCreatedAt()),
				Timestamps.format(user.getUpdatedAt()));
	}
}
