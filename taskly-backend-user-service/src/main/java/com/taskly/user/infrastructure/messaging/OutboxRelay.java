package com.taskly.user.infrastructure.messaging;

import com.taskly.user.application.OutboxRelayService;
import com.taskly.user.application.OutboxStore;
import com.taskly.user.config.TasklyProperties;
import com.taskly.user.shared.domain.Timestamps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnProperty(prefix = "taskly.messaging.outbox", name = "relay-enabled", havingValue = "true")
public class OutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

	private static final int TRANSACTION_TIMEOUT_SECONDS = 30;

	private final OutboxRelayService relayService;

	private final OutboxStore store;

	private final TransactionTemplate transaction;

	private final TasklyProperties properties;

	public OutboxRelay(OutboxRelayService relayService, OutboxStore store,
			PlatformTransactionManager transactionManager, TasklyProperties properties) {
		this.relayService = relayService;
		this.store = store;
		this.properties = properties;
		this.transaction = new TransactionTemplate(transactionManager);
		this.transaction.setTimeout(TRANSACTION_TIMEOUT_SECONDS);
	}

	@Scheduled(fixedDelayString = "${taskly.messaging.outbox.poll-interval-ms}")
	public void relay() {
		try {
			Integer published = transaction.execute(status -> relayService.relay(properties.messaging().outbox().batchSize()));
			if (published != null && published > 0) {
				log.info("Outbox relayed {} event(s)", published);
			}
		}
		catch (RuntimeException error) {
			log.warn("Outbox relay failed: {}", error.getMessage(), error);
		}
	}

	@Scheduled(fixedDelayString = "${taskly.messaging.outbox.cleanup-interval-ms}",
			initialDelayString = "${taskly.messaging.outbox.cleanup-interval-ms}")
	public void cleanup() {
		try {
			Integer removed = transaction.execute(status -> store
					.deletePublishedBefore(Timestamps.now().minus(properties.messaging().outbox().retention())));
			if (removed != null && removed > 0) {
				log.info("Outbox removed {} published event(s)", removed);
			}
		}
		catch (RuntimeException error) {
			log.warn("Outbox cleanup failed: {}", error.getMessage(), error);
		}
	}
}
