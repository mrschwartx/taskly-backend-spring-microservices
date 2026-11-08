package com.taskly.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServiceDiscoveryApplicationTest {

	@Value("${local.server.port}")
	private int port;

	private int status(String path) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
				.header("Accept", "application/json")
				.build();
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
	}

	@Test
	void exposesTheHealthEndpoint() throws Exception {
		assertThat(status("/actuator/health")).isEqualTo(200);
	}

	@Test
	void exposesTheEurekaRegistry() throws Exception {
		assertThat(status("/eureka/apps")).isEqualTo(200);
	}
}
