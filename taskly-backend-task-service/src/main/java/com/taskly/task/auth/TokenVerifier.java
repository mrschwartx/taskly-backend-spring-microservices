package com.taskly.task.auth;

import java.util.UUID;

public interface TokenVerifier {

	UUID verify(String token);
}
