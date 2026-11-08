package com.taskly.user.domain;

import java.util.UUID;

public interface TokenService {

	String issue(UUID userId);

	UUID verify(String token);
}
