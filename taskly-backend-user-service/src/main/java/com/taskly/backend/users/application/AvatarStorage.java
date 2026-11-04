package com.taskly.backend.users.application;

import com.taskly.backend.users.domain.AvatarImageType;

public interface AvatarStorage {

	String store(AvatarImageType type, byte[] content);

	void remove(String avatarPath);
}
