package com.taskly.user.support;

import com.taskly.user.application.AvatarStorage;
import com.taskly.user.domain.AvatarImageType;
import java.util.ArrayList;
import java.util.List;

public class FakeAvatarStorage implements AvatarStorage {

	public final List<String> stored = new ArrayList<>();

	public final List<String> removed = new ArrayList<>();

	@Override
	public String store(AvatarImageType type, byte[] content) {
		String path = "/images/users/avatar-" + stored.size() + type.extension();
		stored.add(path);
		return path;
	}

	@Override
	public void remove(String avatarPath) {
		removed.add(avatarPath);
	}
}
