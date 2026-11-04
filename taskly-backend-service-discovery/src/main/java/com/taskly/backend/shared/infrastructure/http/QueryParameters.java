package com.taskly.backend.shared.infrastructure.http;

import java.util.List;
import org.springframework.util.MultiValueMap;

public final class QueryParameters {

	private QueryParameters() {
	}

	public static Object value(MultiValueMap<String, String> parameters, String key) {
		List<String> values = parameters.get(key);
		if (values == null || values.isEmpty()) {
			return null;
		}
		return values.size() == 1 ? values.get(0) : List.copyOf(values);
	}
}
