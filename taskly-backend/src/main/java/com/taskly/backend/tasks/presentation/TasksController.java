package com.taskly.backend.tasks.presentation;

import com.taskly.backend.shared.application.AuthenticatedUser;
import com.taskly.backend.shared.infrastructure.http.QueryParameters;
import com.taskly.backend.shared.infrastructure.http.RequestBodies;
import com.taskly.backend.tasks.application.CreateTask;
import com.taskly.backend.tasks.application.DeleteTask;
import com.taskly.backend.tasks.application.GetTask;
import com.taskly.backend.tasks.application.ListTasksByUser;
import com.taskly.backend.tasks.application.TaskDto;
import com.taskly.backend.tasks.application.TaskListDto;
import com.taskly.backend.tasks.application.UpdateTask;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
public class TasksController {

	private final CreateTask createTask;

	private final GetTask getTask;

	private final ListTasksByUser listTasksByUser;

	private final UpdateTask updateTask;

	private final DeleteTask deleteTask;

	public TasksController(CreateTask createTask, GetTask getTask, ListTasksByUser listTasksByUser,
			UpdateTask updateTask, DeleteTask deleteTask) {
		this.createTask = createTask;
		this.getTask = getTask;
		this.listTasksByUser = listTasksByUser;
		this.updateTask = updateTask;
		this.deleteTask = deleteTask;
	}

	@GetMapping("/user/{id}")
	public TaskListDto listByUser(@AuthenticationPrincipal AuthenticatedUser requester, @PathVariable String id,
			@RequestParam MultiValueMap<String, String> parameters) {
		return listTasksByUser.execute(requester.userId(), id, QueryParameters.value(parameters, "status"),
				QueryParameters.value(parameters, "orderBy"), QueryParameters.value(parameters, "page"));
	}

	@GetMapping("/{id}")
	public TaskDto get(@AuthenticationPrincipal AuthenticatedUser requester, @PathVariable String id) {
		return getTask.execute(requester.userId(), id);
	}

	@PostMapping("/create")
	public TaskDto create(@AuthenticationPrincipal AuthenticatedUser requester,
			@RequestBody(required = false) Map<String, Object> body) {
		return createTask.execute(requester.userId(), RequestBodies.orEmpty(body));
	}

	@PatchMapping("/{id}")
	public TaskDto update(@AuthenticationPrincipal AuthenticatedUser requester, @PathVariable String id,
			@RequestBody(required = false) Map<String, Object> body) {
		return updateTask.execute(requester.userId(), id, RequestBodies.orEmpty(body));
	}

	@DeleteMapping("/{id}")
	public Map<String, String> delete(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String id) {
		deleteTask.execute(requester.userId(), id);
		return Map.of("message", "Task has been deleted");
	}
}
