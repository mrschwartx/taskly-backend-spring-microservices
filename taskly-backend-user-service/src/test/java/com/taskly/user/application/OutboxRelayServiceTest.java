package com.taskly.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.taskly.user.shared.domain.EntityIds;
import com.taskly.user.shared.domain.Timestamps;
import com.taskly.user.support.FakeMessageSender;
import com.taskly.user.support.InMemoryOutboxStore;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OutboxRelayServiceTest {

	private InMemoryOutboxStore store;

	private FakeMessageSender sender;

	private OutboxRelayService relay;

	@BeforeEach
	void setUp() {
		store = new InMemoryOutboxStore();
		sender = new FakeMessageSender();
		relay = new OutboxRelayService(store, sender);
	}

	private OutboxMessage message(String key) {
		return new OutboxMessage(EntityIds.generate(), key, "user.deleted", "{}", "corr-0001", Timestamps.now().minusSeconds(1), 0);
	}

	@Test
	void relaysPendingMessagesInOrderAndMarksThemPublished() {
		store.append(message("a"));
		store.append(message("b"));

		int published = relay.relay(10);

		assertThat(published).isEqualTo(2);
		assertThat(sender.sent).extracting(OutboxMessage::key).containsExactly("a", "b");
		assertThat(store.entries).allSatisfy(entry -> assertThat(entry.publishedAt).isNotNull());
	}

	@Test
	void respectsTheBatchSize() {
		store.append(message("a"));
		store.append(message("b"));
		store.append(message("c"));

		assertThat(relay.relay(2)).isEqualTo(2);
		assertThat(relay.relay(2)).isEqualTo(1);
		assertThat(sender.sent).hasSize(3);
	}

	@Test
	void stopsAtTheFirstFailureAndSchedulesARetry() {
		store.append(message("a"));
		store.append(message("b"));
		sender.failing = true;
		Instant before = Timestamps.now();

		int published = relay.relay(10);

		assertThat(published).isZero();
		assertThat(sender.sent).isEmpty();
		InMemoryOutboxStore.Entry first = store.entries.get(0);
		InMemoryOutboxStore.Entry second = store.entries.get(1);
		assertThat(first.message.attempts()).isEqualTo(1);
		assertThat(first.nextAttemptAt).isAfter(before);
		assertThat(first.publishedAt).isNull();
		assertThat(second.message.attempts()).isZero();
	}

	@Test
	void skipsMessagesWhoseRetryIsNotDueYet() {
		store.append(message("a"));
		sender.failing = true;
		relay.relay(10);
		sender.failing = false;

		assertThat(relay.relay(10)).isZero();
		assertThat(sender.sent).isEmpty();
	}

	@Test
	void backoffGrowsExponentiallyAndIsCapped() {
		assertThat(OutboxRelayService.backoff(1)).isEqualTo(Duration.ofSeconds(2));
		assertThat(OutboxRelayService.backoff(2)).isEqualTo(Duration.ofSeconds(4));
		assertThat(OutboxRelayService.backoff(5)).isEqualTo(Duration.ofSeconds(32));
		assertThat(OutboxRelayService.backoff(6)).isEqualTo(Duration.ofSeconds(60));
		assertThat(OutboxRelayService.backoff(50)).isEqualTo(Duration.ofSeconds(60));
	}

	@Test
	void cleanupRemovesOnlyOldPublishedMessages() {
		store.append(message("a"));
		store.append(message("b"));
		relay.relay(1);

		assertThat(store.deletePublishedBefore(Timestamps.now().plusSeconds(5))).isEqualTo(1);
		assertThat(store.entries).hasSize(1);
	}
}
