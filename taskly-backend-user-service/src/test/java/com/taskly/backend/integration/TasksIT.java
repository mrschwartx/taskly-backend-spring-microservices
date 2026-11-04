package com.taskly.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class TasksIT extends IntegrationTestSupport {

	private static final String MISSING_ID = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void requiresAuthenticationOnEveryRoute() {
		ApiClient anonymous = client();

		assertThat(anonymous.get("/api/v1/tasks/" + MISSING_ID).status()).isEqualTo(401);
		assertThat(anonymous.get("/api/v1/tasks/user/" + MISSING_ID).status()).isEqualTo(401);
		assertThat(anonymous.post("/api/v1/tasks/create", taskJson("x", "todo")).status()).isEqualTo(401);
		assertThat(anonymous.patch("/api/v1/tasks/" + MISSING_ID, "{}").status()).isEqualTo(401);
		assertThat(anonymous.delete("/api/v1/tasks/" + MISSING_ID).status()).isEqualTo(401);
	}

	@Test
	void createsATaskOwnedByTheAuthenticatedUser() {
		Registered user = register();
		Registered other = register();

		ApiResponse response = user.client().post("/api/v1/tasks/create",
				ApiClient.json("name", "Read Atomic Habits", "description", "Finish", "priority", "low", "due",
						"2026-12-08T10:00:00Z", "owner", other.id()));

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$._id")).matches("^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$");
		assertThat((String) response.json("$.owner")).isEqualTo(user.id());
		assertThat((String) response.json("$.status")).isEqualTo("todo");
		assertThat((String) response.json("$.due")).isEqualTo("2026-12-08T10:00:00.000Z");
		assertThat((String) response.json("$.createdAt")).isEqualTo(response.json("$.updatedAt"));
	}

	@Test
	void createRejectsInvalidPayloads() {
		Registered user = register();
		String[] payloads = {
				"{}",
				ApiClient.json("name", "", "priority", "low", "due", "2026-12-08T10:00:00Z"),
				ApiClient.json("name", "x", "priority", "low", "due", "someday"),
				ApiClient.json("name", "x", "due", "2026-12-08T10:00:00Z") };

		for (String payload : payloads) {
			assertThat(user.client().post("/api/v1/tasks/create", payload).status()).isEqualTo(400);
		}
	}

	@Test
	void getsATaskForItsOwnerOnly() {
		Registered owner = register();
		Registered intruder = register();
		ApiResponse created = owner.client().post("/api/v1/tasks/create", taskJson("Read", "todo"));
		String id = created.json("$._id");

		ApiResponse found = owner.client().get("/api/v1/tasks/" + id);

		assertThat(found.status()).isEqualTo(200);
		assertThat(found.body()).isEqualTo(created.body());
		assertThat(intruder.client().get("/api/v1/tasks/" + id).status()).isEqualTo(403);
		ApiResponse unknown = owner.client().get("/api/v1/tasks/" + MISSING_ID);
		assertThat(unknown.status()).isEqualTo(404);
		assertThat((String) unknown.json("$.message")).isEqualTo("Task not found!");
		assertThat(owner.client().get("/api/v1/tasks/nope").status()).isEqualTo(400);
	}

	@Test
	void paginatesFourTasksPerPageInCreationOrder() {
		Registered user = register();
		for (int index = 1; index <= 6; index++) {
			user.client().post("/api/v1/tasks/create", taskJson("Task " + index, "todo"));
		}

		ApiResponse first = user.client().get("/api/v1/tasks/user/" + user.id());
		ApiResponse second = user.client().get("/api/v1/tasks/user/" + user.id() + "?page=2");

		assertThat(first.status()).isEqualTo(200);
		assertThat((Integer) first.json("$.taskCount")).isEqualTo(6);
		assertThat((Integer) first.json("$.tasks.length()")).isEqualTo(4);
		assertThat((Integer) second.json("$.tasks.length()")).isEqualTo(2);
		List<String> firstNames = first.json("$.tasks[*].name");
		List<String> secondNames = second.json("$.tasks[*].name");
		List<String> names = new ArrayList<>(firstNames);
		names.addAll(secondNames);
		assertThat(names).containsExactly("Task 1", "Task 2", "Task 3", "Task 4", "Task 5", "Task 6");
	}

	@Test
	void filtersByStatusAndOrdersByField() {
		Registered user = register();
		user.client().post("/api/v1/tasks/create", taskJson("b-task", "done"));
		user.client().post("/api/v1/tasks/create", taskJson("a-task", "done"));
		user.client().post("/api/v1/tasks/create", taskJson("c-task", "todo"));

		ApiResponse response = user.client().get("/api/v1/tasks/user/" + user.id() + "?status=done&orderBy=name");

		assertThat((Integer) response.json("$.taskCount")).isEqualTo(2);
		List<String> names = response.json("$.tasks[*].name");
		assertThat(names).containsExactly("a-task", "b-task");
	}

	@Test
	void listValidatesAccessAndParameters() {
		Registered owner = register();
		Registered intruder = register();
		String path = "/api/v1/tasks/user/" + owner.id();

		ApiResponse empty = owner.client().get(path);

		assertThat((Integer) empty.json("$.taskCount")).isZero();
		assertThat(empty.body()).isEqualTo("{\"tasks\":[],\"taskCount\":0}");
		assertThat(intruder.client().get(path).status()).isEqualTo(403);
		assertThat(owner.client().get(path + "?orderBy=owner").status()).isEqualTo(400);
		assertThat(owner.client().get(path + "?status=a&status=b").status()).isEqualTo(400);
		assertThat(owner.client().get("/api/v1/tasks/user/not-an-id").status()).isEqualTo(400);
	}

	@Test
	void updatesATaskWithoutChangingItsOwner() {
		Registered owner = register();
		Registered other = register();
		ApiResponse created = owner.client().post("/api/v1/tasks/create", taskJson("Read", "todo"));
		String id = created.json("$._id");

		ApiResponse response = owner.client().patch("/api/v1/tasks/" + id,
				ApiClient.json("name", "Read - Updated", "priority", "high", "status", "in_progress", "due",
						"2026-12-15T10:00:00Z", "owner", other.id()));

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.name")).isEqualTo("Read - Updated");
		assertThat((String) response.json("$.status")).isEqualTo("in_progress");
		assertThat((String) response.json("$.due")).isEqualTo("2026-12-15T10:00:00.000Z");
		assertThat((String) response.json("$.owner")).isEqualTo(owner.id());
		assertThat((String) response.json("$.createdAt")).isEqualTo(created.json("$.createdAt"));
	}

	@Test
	void updateRejectsForeignEmptyAndInvalidChanges() {
		Registered owner = register();
		Registered intruder = register();
		String id = owner.client().post("/api/v1/tasks/create", taskJson("Read", "todo")).json("$._id");
		String path = "/api/v1/tasks/" + id;

		assertThat(intruder.client().patch(path, ApiClient.json("name", "Hacked")).status()).isEqualTo(403);
		assertThat((String) owner.client().get(path).json("$.name")).isEqualTo("Read");
		assertThat(owner.client().patch(path, "{}").status()).isEqualTo(400);
		assertThat(owner.client().patch(path, ApiClient.json("due", "never")).status()).isEqualTo(400);
	}

	@Test
	void deletesATaskForItsOwnerOnly() {
		Registered owner = register();
		Registered intruder = register();
		String id = owner.client().post("/api/v1/tasks/create", taskJson("Read", "todo")).json("$._id");
		String path = "/api/v1/tasks/" + id;

		assertThat(intruder.client().delete(path).status()).isEqualTo(403);
		assertThat(owner.client().get(path).status()).isEqualTo(200);

		ApiResponse deleted = owner.client().delete(path);

		assertThat(deleted.status()).isEqualTo(200);
		assertThat((String) deleted.json("$.message")).isEqualTo("Task has been deleted");
		assertThat(owner.client().get(path).status()).isEqualTo(404);
		assertThat(owner.client().delete("/api/v1/tasks/" + MISSING_ID).status()).isEqualTo(404);
	}

	@Test
	void deletingAUserRemovesOnlyThatUsersTasks() {
		Registered leaving = register();
		Registered staying = register();
		leaving.client().post("/api/v1/tasks/create", taskJson("One", "todo"));
		leaving.client().post("/api/v1/tasks/create", taskJson("Two", "todo"));
		staying.client().post("/api/v1/tasks/create", taskJson("Kept", "todo"));

		leaving.client().delete("/api/v1/users/delete/" + leaving.id());

		Integer gone = jdbc.queryForObject("select count(*) from tasks where owner_id = ?::uuid", Integer.class,
				leaving.id());
		ApiResponse kept = staying.client().get("/api/v1/tasks/user/" + staying.id());
		assertThat(gone).isZero();
		assertThat((Integer) kept.json("$.taskCount")).isEqualTo(1);
	}
}
