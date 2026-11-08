package com.taskly.task.infrastructure;

import com.taskly.task.config.TasklyProperties;
import com.taskly.task.domain.DeletedUserRepository;
import com.taskly.task.shared.domain.Timestamps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TombstonePurger {

	private static final Logger log = LoggerFactory.getLogger(TombstonePurger.class);

	private final DeletedUserRepository deletedUsers;

	private final TasklyProperties properties;

	public TombstonePurger(DeletedUserRepository deletedUsers, TasklyProperties properties) {
		this.deletedUsers = deletedUsers;
		this.properties = properties;
	}

	@Scheduled(fixedDelayString = "${taskly.tombstones.purge-interval-ms}",
			initialDelayString = "${taskly.tombstones.purge-interval-ms}")
	public void purge() {
		try {
			int removed = deletedUsers.purgeOlderThan(Timestamps.now().minus(properties.tombstones().retention()));
			if (removed > 0) {
				log.info("Purged {} deleted-user tombstone(s)", removed);
			}
		}
		catch (RuntimeException error) {
			log.warn("Tombstone purge failed: {}", error.getMessage(), error);
		}
	}
}
