package com.taskly.task.application;

import com.taskly.task.shared.domain.EntityIds;
import com.taskly.task.shared.domain.ForbiddenException;
import com.taskly.task.shared.domain.ValidationException;
import com.taskly.task.domain.TaskPage;
import com.taskly.task.domain.TaskRepository;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ListTasksByUser {

	public static final int PAGE_SIZE = 4;

	private static final List<String> SORTABLE_FIELDS = List.of("name", "description", "priority", "due", "status",
			"createdAt", "updatedAt");

	private static final int MAX_PAGE = 1_000_000;

	private static final Pattern LEADING_INTEGER = Pattern.compile("^\\s*([+-]?\\d+)");

	private final TaskRepository taskRepository;

	public ListTasksByUser(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public TaskListDto execute(UUID requesterId, String ownerId, Object status, Object orderBy, Object page) {
		UUID owner = EntityIds.parse(ownerId, "user");
		if (!owner.equals(requesterId)) {
			throw new ForbiddenException("You are not authorized to view these tasks");
		}
		if (status != null && !(status instanceof String)) {
			throw new ValidationException("Status filter must be a string");
		}
		if (orderBy != null && !(orderBy instanceof String field && SORTABLE_FIELDS.contains(field))) {
			throw new ValidationException("Invalid orderBy field");
		}

		String statusFilter = status instanceof String text && !text.isEmpty() ? text : null;
		TaskPage result = taskRepository.findByOwner(owner, statusFilter, (String) orderBy, normalizePage(page),
				PAGE_SIZE);

		return new TaskListDto(result.tasks().stream().map(TaskDto::from).toList(), result.total());
	}

	private static int normalizePage(Object page) {
		if (page == null) {
			return 1;
		}
		String text = page instanceof List<?> values ? String.join(",", values.stream().map(String::valueOf).toList())
				: String.valueOf(page);
		Matcher matcher = LEADING_INTEGER.matcher(text);
		if (!matcher.find()) {
			return 1;
		}
		try {
			long parsed = Long.parseLong(matcher.group(1));
			return (int) Math.min(Math.max(parsed, 1), MAX_PAGE);
		}
		catch (NumberFormatException error) {
			return matcher.group(1).startsWith("-") ? 1 : MAX_PAGE;
		}
	}
}
