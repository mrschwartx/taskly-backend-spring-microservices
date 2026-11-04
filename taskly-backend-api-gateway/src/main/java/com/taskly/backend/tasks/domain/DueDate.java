package com.taskly.backend.tasks.domain;

import com.taskly.backend.shared.domain.TextRules;
import com.taskly.backend.shared.domain.ValidationException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;

public record DueDate(Instant value) {

	private static final int MIN_YEAR = 1;

	private static final int MAX_YEAR = 9999;

	private static final List<Function<String, Instant>> PARSERS = List.of(
			text -> OffsetDateTime.parse(text).toInstant(),
			text -> LocalDateTime.parse(text).toInstant(ZoneOffset.UTC),
			text -> LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC).toInstant(),
			text -> ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());

	public static DueDate create(Object raw) {
		if (!(raw instanceof String text) || TextRules.trim(text).isEmpty()) {
			throw new ValidationException("Task due date is required");
		}
		Instant parsed = parse(TextRules.trim(text));
		if (parsed == null) {
			throw new ValidationException("Task due date is invalid");
		}
		int year = parsed.atOffset(ZoneOffset.UTC).getYear();
		if (year < MIN_YEAR || year > MAX_YEAR) {
			throw new ValidationException("Task due date is invalid");
		}
		return new DueDate(parsed.truncatedTo(ChronoUnit.MILLIS));
	}

	public static DueDate restore(Instant value) {
		return new DueDate(value);
	}

	private static Instant parse(String text) {
		for (Function<String, Instant> parser : PARSERS) {
			try {
				return parser.apply(text);
			}
			catch (DateTimeException | ArithmeticException error) {
				continue;
			}
		}
		return null;
	}
}
