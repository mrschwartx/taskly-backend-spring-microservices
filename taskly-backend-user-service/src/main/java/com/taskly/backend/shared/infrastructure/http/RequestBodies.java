package com.taskly.backend.shared.infrastructure.http;

import java.util.Collections;
import java.util.Map;

public final class RequestBodies {

	private RequestBodies() {
	}

	public static Map<String, Object> orEmpty(Map<String, Object> body) {
		return body != null ? body : Collections.emptyMap();
	}
}
