package com.taskly.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"management.tracing.enabled=false",
		"logging.file.name=" })
class RoutesConfigTest {

	@Autowired
	private RouteLocator routeLocator;

	private Route route(List<Route> routes, String id) {
		return routes.stream().filter(route -> route.getId().equals(id)).findFirst().orElseThrow();
	}

	@Test
	void exposesTheMonolithApiContractThroughServiceRoutes() {
		List<Route> routes = routeLocator.getRoutes().collectList().block();

		assertThat(routes).extracting(Route::getId).containsExactlyInAnyOrder("auth-credentials", "auth",
				"users-upload", "users", "tasks", "avatars");
		assertThat(route(routes, "tasks").getUri()).hasToString("lb://task-service");
		assertThat(route(routes, "users").getUri()).hasToString("lb://user-service");
		assertThat(route(routes, "avatars").getUri()).hasToString("lb://user-service");
		assertThat(route(routes, "auth-credentials").getUri()).hasToString("lb://user-service");
	}

	@Test
	void everyRouteHasExplicitTimeouts() {
		List<Route> routes = routeLocator.getRoutes().collectList().block();

		assertThat(routes).allSatisfy(route -> {
			assertThat(route.getMetadata()).containsEntry("connect-timeout", 1000);
			assertThat(route.getMetadata()).containsKey("response-timeout");
		});
		assertThat(route(routes, "tasks").getMetadata()).containsEntry("response-timeout", 4000);
		assertThat(route(routes, "users-upload").getMetadata()).containsEntry("response-timeout", 15000);
	}
}
