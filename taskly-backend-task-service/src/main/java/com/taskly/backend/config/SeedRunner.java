package com.taskly.backend.config;

import com.taskly.backend.shared.domain.AlreadyExistsException;
import com.taskly.backend.shared.domain.Timestamps;
import com.taskly.backend.tasks.application.CreateTask;
import com.taskly.backend.users.application.AuthResult;
import com.taskly.backend.users.application.RegisterUser;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "taskly.seed.enabled", havingValue = "true")
public class SeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

	private record SeedEntry(Map<String, Object> user, List<Map<String, Object>> tasks) {
	}

	private final TasklyProperties properties;

	private final RegisterUser registerUser;

	private final CreateTask createTask;

	private final ApplicationContext context;

	public SeedRunner(TasklyProperties properties, RegisterUser registerUser, CreateTask createTask,
			ApplicationContext context) {
		this.properties = properties;
		this.registerUser = registerUser;
		this.createTask = createTask;
		this.context = context;
	}

	@Override
	public void run(ApplicationArguments args) {
		int exitCode = seed();
		System.exit(SpringApplication.exit(context, () -> exitCode));
	}

	private int seed() {
		if (properties.app().production() && !properties.seed().allowProduction()) {
			log.error("Refusing to seed a production database");
			return 1;
		}
		try {
			for (SeedEntry entry : entries()) {
				seedEntry(entry);
			}
			log.info("Seeding completed");
			return 0;
		}
		catch (RuntimeException error) {
			log.error("Seeding failed: {}", error.getMessage(), error);
			return 1;
		}
	}

	private void seedEntry(SeedEntry entry) {
		String username = String.valueOf(entry.user().get("username"));
		try {
			AuthResult result = registerUser.execute(entry.user());
			UUID ownerId = UUID.fromString(result.user().id());
			for (Map<String, Object> task : entry.tasks()) {
				createTask.execute(ownerId, task);
			}
			log.info("Seeded user {}", username);
		}
		catch (AlreadyExistsException error) {
			log.warn("Skipped existing user {}", username);
		}
	}

	private static List<SeedEntry> entries() {
		String now = Timestamps.format(Timestamps.now());
		return List.of(
				new SeedEntry(
						Map.of("username", "johndoe123", "email", "johndoe123@mail.com", "password", "password123"),
						List.of(Map.of(
								"name", "Read Atomic Habits",
								"description", "Finish reading Atomic Habits by James Clear",
								"priority", "low",
								"due", now,
								"status", "todo"))),
				new SeedEntry(
						Map.of("username", "jane78", "email", "jane@mail.com", "password", "securepassword456"),
						List.of(Map.of(
								"name", "Learn MERN Stack",
								"description", "Learn the MERN stack and build a full-stack application with it",
								"priority", "high",
								"due", now,
								"status", "todo"))));
	}
}
