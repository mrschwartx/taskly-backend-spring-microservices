package com.taskly.task.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taskly.task.shared.domain.EntityIds;
import com.taskly.task.shared.domain.ValidationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EntityIdsTest {

	@Test
	void generatesValidIds() {
		assertThat(EntityIds.isValid(EntityIds.generate().toString())).isTrue();
	}

	@Test
	void parsesAndLowercasesIds() {
		UUID id = EntityIds.parse("AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA", "task");
		assertThat(id).hasToString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	}

	@Test
	void rejectsMalformedIdsWithLabel() {
		assertThatThrownBy(() -> EntityIds.parse("nope", "user"))
				.isInstanceOf(ValidationException.class)
				.hasMessage("Invalid user ID format");
		assertThatThrownBy(() -> EntityIds.parse(null, "task")).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> EntityIds.parse(42, "task")).isInstanceOf(ValidationException.class);
	}
}
