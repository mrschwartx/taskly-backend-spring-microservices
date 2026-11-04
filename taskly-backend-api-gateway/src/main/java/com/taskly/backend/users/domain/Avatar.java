package com.taskly.backend.users.domain;

import java.util.regex.Pattern;

public final class Avatar {

	public static final String URL_PREFIX = "/images/users/";

	public static final String DEFAULT_PATH = URL_PREFIX + "default-avatar.jpg";

	private static final Pattern CUSTOM_AVATAR_PATTERN = Pattern.compile("^/images/users/avatar-[\\w.-]+$");

	private Avatar() {
	}

	public static boolean isCustom(String avatarPath) {
		return avatarPath != null && CUSTOM_AVATAR_PATTERN.matcher(avatarPath).matches();
	}

	public static String toPath(String filename) {
		return URL_PREFIX + filename;
	}
}
