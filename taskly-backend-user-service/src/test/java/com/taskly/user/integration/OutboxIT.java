package com.taskly.user.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class OutboxIT extends IntegrationTestSupport {

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void deletingAUserStoresAUserDeletedEventInTheOutbox() {
		Registered user = register();

		ApiResponse response = user.client().delete("/api/v1/users/delete/" + user.id(),
				Map.of("X-Correlation-Id", "it-correlation-0001"));

		assertThat(response.status()).isEqualTo(200);
		assertThat(response.header("x-correlation-id")).isEqualTo("it-correlation-0001");
		Map<String, Object> row = jdbc.queryForMap(
				"select event_key, event_name, payload, correlation_id, published_at from outbox_events where event_key = ?",
				user.id());
		assertThat(row.get("event_name")).isEqualTo("user.deleted");
		assertThat(row.get("correlation_id")).isEqualTo("it-correlation-0001");
		assertThat(row.get("published_at")).isNull();
		assertThat((String) row.get("payload")).contains(user.id());
	}

	@Test
	void doesNotStoreAnEventWhenTheDeletionIsRejected() {
		Registered victim = register();
		Registered attacker = register();

		ApiResponse response = attacker.client().delete("/api/v1/users/delete/" + victim.id());

		assertThat(response.status()).isEqualTo(401);
		Integer events = jdbc.queryForObject("select count(*) from outbox_events where event_key = ?", Integer.class,
				victim.id());
		assertThat(events).isZero();
	}
}
