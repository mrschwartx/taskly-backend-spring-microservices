package com.taskly.backend.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

final class ApiClient {

	private final String baseUrl;

	private final CookieManager cookies = new CookieManager();

	private final HttpClient http;

	ApiClient(String baseUrl) {
		this.baseUrl = baseUrl;
		this.http = HttpClient.newBuilder().cookieHandler(cookies).build();
	}

	String baseUrl() {
		return baseUrl;
	}

	boolean hasCookie(String name) {
		return cookies.getCookieStore().getCookies().stream().anyMatch(cookie -> cookie.getName().equals(name));
	}

	ApiResponse get(String path) {
		return send("GET", path, null, null, Map.of());
	}

	ApiResponse get(String path, Map<String, String> headers) {
		return send("GET", path, null, null, headers);
	}

	ApiResponse post(String path, String json) {
		return send("POST", path, json, "application/json", Map.of());
	}

	ApiResponse patch(String path, String json) {
		return send("PATCH", path, json, "application/json", Map.of());
	}

	ApiResponse delete(String path) {
		return send("DELETE", path, null, null, Map.of());
	}

	ApiResponse options(String path, Map<String, String> headers) {
		return send("OPTIONS", path, null, null, headers);
	}

	ApiResponse rawPost(String path, String body, String contentType) {
		return send("POST", path, body, contentType, Map.of());
	}

	ApiResponse upload(String path, String field, String filename, String contentType, byte[] content) {
		String boundary = "----taskly" + UUID.randomUUID();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(out, "--" + boundary + "\r\n");
		write(out, "Content-Disposition: form-data; name=\"" + field + "\"; filename=\"" + filename + "\"\r\n");
		write(out, "Content-Type: " + contentType + "\r\n\r\n");
		out.writeBytes(content);
		write(out, "\r\n--" + boundary + "--\r\n");
		return execute(
				builder("POST", path, Map.of()).header("Content-Type", "multipart/form-data; boundary=" + boundary)
						.POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray())));
	}

	ApiResponse emptyMultipart(String path) {
		String boundary = "----taskly" + UUID.randomUUID();
		return execute(
				builder("POST", path, Map.of()).header("Content-Type", "multipart/form-data; boundary=" + boundary)
						.POST(HttpRequest.BodyPublishers.ofString("--" + boundary + "--\r\n")));
	}

	private ApiResponse send(String method, String path, String body, String contentType,
			Map<String, String> headers) {
		HttpRequest.Builder builder = builder(method, path, headers);
		if (contentType != null) {
			builder.header("Content-Type", contentType);
		}
		builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofString(body));
		return execute(builder);
	}

	private HttpRequest.Builder builder(String method, String path, Map<String, String> headers) {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path));
		headers.forEach(builder::header);
		return builder;
	}

	private ApiResponse execute(HttpRequest.Builder builder) {
		try {
			HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
			return new ApiResponse(response.statusCode(), response.body(), response.headers());
		}
		catch (IOException error) {
			throw new IllegalStateException(error);
		}
		catch (InterruptedException error) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(error);
		}
	}

	private static void write(ByteArrayOutputStream out, String text) {
		out.writeBytes(text.getBytes(StandardCharsets.UTF_8));
	}

	static String json(Object... keysAndValues) {
		StringBuilder builder = new StringBuilder("{");
		for (int index = 0; index < keysAndValues.length; index += 2) {
			if (index > 0) {
				builder.append(',');
			}
			builder.append(quote(String.valueOf(keysAndValues[index]))).append(':').append(value(keysAndValues[index + 1]));
		}
		return builder.append('}').toString();
	}

	private static String value(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		}
		return quote(String.valueOf(value));
	}

	private static String quote(String text) {
		return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
}
