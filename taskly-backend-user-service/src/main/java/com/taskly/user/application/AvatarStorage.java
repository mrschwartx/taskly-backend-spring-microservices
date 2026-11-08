package com.taskly.user.application;

import com.taskly.user.domain.AvatarImageType;

public interface AvatarStorage {

	String store(AvatarImageType type, byte[] content);

	void remove(String avatarPath);
}
