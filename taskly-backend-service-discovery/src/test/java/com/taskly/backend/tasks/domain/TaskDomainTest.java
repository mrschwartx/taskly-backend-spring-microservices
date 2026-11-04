package com.taskly.backend.tasks.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taskly.backend.shared.domain.ValidationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaskDomainTest {

	private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");

	@Test
	void dueDateParsesIsoFormatsToUtcMilliseconds() {
		assertThat(DueDate.create("2026-12-08T10:00:00Z").value()).isEqualTo(Instant.parse("2026-12-08T10:00:00Z"));
		assertThat(DueDate.create("2026-12-08T17:00:00+07:00").value()).isEqualTo(Instant.parse("2026-12-08T10:00:00Z"));
		assertThat(DueDate.create("2026-12-08T10:00:00").value()).isEqualTo(Instant.parse("2026-12-08T10:00:00Z"));
		assertThat(DueDate.create("2026-12-08").value()).isEqualTo(Instant.parse("2026-12-08T00:00:00Z"));
		assertThat(DueDate.create("2026-12-08T10:00:00.123456Z").value())
				.isEqualTo(Instant.parse("2026-12-08T10:00:00.123Z"));
	}

	@Test
	void dueDateRejectsMissingOrInvalidValues() {
		assertThatThrownBy(() -> DueDate.create(null)).hasMessage("Task due date is required");
		assertThatThrownBy(() -> DueDate.create("   ")).hasMessage("Task due date is required");
		assertThatThrownBy(() -> DueDate.create(123)).hasMessage("Task due date is required");
		assertThatThrownBy(() -> DueDate.create("someday")).hasMessage("Task due date is invalid");
		assertThatThrownBy(() -> DueDate.create("2026-13-45")).hasMessage("Task due date is invalid");
	}

	@Test
	void textValueObjectsValidateLengthAndAllowedValues() {
		assertThat(TaskName.create("  Read ").value()).isEqualTo("Read");
		assertThatThrownBy(() -> TaskName.create("")).hasMessage("Task name must be between 1 and 120 characters");
		assertThatThrownBy(() -> TaskName.create("a".repeat(121))).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> TaskName.create(null)).hasMessage("Task name is required");
		assertThat(TaskDescription.create("").value()).isEmpty();
		assertThatThrownBy(() -> TaskDescription.create("a".repeat(2001))).isInstanceOf(ValidationException.class);
		assertThat(TaskPriority.create("high")).isEqualTo(TaskPriority.HIGH);
		assertThatThrownBy(() -> TaskPriority.create("urgent"))
				.hasMessage("Task priority must be one of: low, medium, high");
		assertThat(TaskStatus.create("in_progress")).isEqualTo(TaskStatus.IN_PROGRESS);
		assertThatThrownBy(() -> TaskStatus.create("finished"))
				.hasMessage("Task status must be one of: todo, in_progress, done");
		assertThat(TaskStatus.defaultStatus()).isEqualTo(TaskStatus.TODO);
	}

	private Task newTask(UUID owner) {
		return Task.create(UUID.randomUUID(), owner, TaskName.create("Read"), TaskDescription.empty(),
				TaskPriority.LOW, DueDate.create("2026-12-08T10:00:00Z"), TaskStatus.TODO, CREATED);
	}

	@Test
	void taskUpdateAppliesOnlyProvidedChanges() {
		UUID owner = UUID.randomUUID();
		Task task = newTask(owner);
		Instant later = CREATED.plusSeconds(30);

		task.update(new TaskChanges(null, null, TaskPriority.HIGH, null, TaskStatus.DONE), later);

		assertThat(task.getName().value()).isEqualTo("Read");
		assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
		assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
		assertThat(task.getCreatedAt()).isEqualTo(CREATED);
		assertThat(task.getUpdatedAt()).isEqualTo(later);
		assertThat(task.isOwnedBy(owner)).isTrue();
		assertThat(task.isOwnedBy(UUID.randomUUID())).isFalse();
	}

	@Test
	void emptyChangesAreDetected() {
		assertThat(new TaskChanges(null, null, null, null, null).isEmpty()).isTrue();
		assertThat(new TaskChanges(TaskName.create("x"), null, null, null, null).isEmpty()).isFalse();
	}
}
