package com.taskly.backend.integration;

import com.jayway.jsonpath.JsonPath;
import java.net.http.HttpHeaders;
import java.util.List;

record ApiResponse(int status, String body, HttpHeaders headers) {

	<T> T json(String path) {
		return JsonPath.read(body, path);
	}

	List<String> setCookies() {
		return headers.allValues("set-cookie");
	}

	String header(String name) {
		return headers.firstValue(name).orElse(null);
	}
}
