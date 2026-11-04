package com.taskly.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthAndUsersIT extends IntegrationTestSupport {

	private static final String MISSING_ID = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";

	private static final byte[] PNG = new byte[] { (byte) 0x89, 'P', 'N', 'G', 1, 2, 3 };

	@Test
	void signUpCreatesAUserAndStartsASession() {
		ApiClient client = client();
		String body = ApiClient.json("username", "signup_user", "email", "signup@mail.com", "password",
				"Password123!");

		ApiResponse response = client.post("/api/v1/auth/signup", body);

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.username")).isEqualTo("signup_user");
		assertThat((String) response.json("$.avatar")).isEqualTo("/images/users/default-avatar.jpg");
		assertThat((String) response.json("$._id")).matches("^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$");
		assertThat(response.body()).doesNotContain("password");
		assertThat(client.hasCookie("taskly_token")).isTrue();
		String cookie = response.setCookies().stream().filter(value -> value.startsWith("taskly_token=")).findFirst()
				.orElseThrow();
		assertThat(cookie).containsIgnoringCase("HttpOnly").containsIgnoringCase("SameSite=Lax")
				.containsIgnoringCase("Max-Age=3600");
	}

	@Test
	void signUpRejectsDuplicatesWith422() {
		Registered existing = register();

		ApiResponse sameEmail = client().post("/api/v1/auth/signup", ApiClient.json("username", "other_" + UUID.randomUUID().toString().substring(0, 6),
				"email", existing.email().toUpperCase(), "password", "Password123!"));
		ApiResponse sameUsername = client().post("/api/v1/auth/signup", ApiClient.json("username", existing.username(),
				"email", "fresh@mail.com", "password", "Password123!"));

		for (ApiResponse response : new ApiResponse[] { sameEmail, sameUsername }) {
			assertThat(response.status()).isEqualTo(422);
			assertThat((String) response.json("$.message")).isEqualTo("Email or Username is already registered.");
			assertThat((Integer) response.json("$.status")).isEqualTo(422);
		}
	}

	@Test
	void signUpRejectsInvalidPayloadsWith400() {
		ApiClient client = client();
		String[] payloads = {
				"{}",
				ApiClient.json("username", "ab", "email", "a@mail.com", "password", "Password123!"),
				ApiClient.json("username", "valid_user", "email", "nope", "password", "Password123!"),
				ApiClient.json("username", "valid_user", "email", "a@mail.com", "password", "short") };

		for (String payload : payloads) {
			assertThat(client.post("/api/v1/auth/signup", payload).status()).isEqualTo(400);
		}
	}

	@Test
	void signInHandlesSuccessAndFailures() {
		Registered user = register();

		ApiClient fresh = client();
		ApiResponse ok = fresh.post("/api/v1/auth/signin",
				ApiClient.json("email", user.email(), "password", user.password()));
		ApiResponse unknown = client().post("/api/v1/auth/signin",
				ApiClient.json("email", "ghost@mail.com", "password", "Password123!"));
		ApiResponse wrong = client().post("/api/v1/auth/signin",
				ApiClient.json("email", user.email(), "password", "definitely-wrong"));
		ApiClient injected = client();
		ApiResponse injection = injected.post("/api/v1/auth/signin",
				"{\"email\":{\"$ne\":null},\"password\":\"Password123!\"}");

		assertThat(ok.status()).isEqualTo(200);
		assertThat((String) ok.json("$._id")).isEqualTo(user.id());
		assertThat(fresh.hasCookie("taskly_token")).isTrue();
		assertThat(unknown.status()).isEqualTo(404);
		assertThat((String) unknown.json("$.message")).isEqualTo("User not found!");
		assertThat(wrong.status()).isEqualTo(401);
		assertThat((String) wrong.json("$.message")).isEqualTo("Wrong password!");
		assertThat(injection.status()).isEqualTo(400);
		assertThat(injected.hasCookie("taskly_token")).isFalse();
	}

	@Test
	void signOutClearsTheSession() {
		Registered user = register();

		ApiResponse response = user.client().get("/api/v1/auth/signout");

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.message")).isEqualTo("Sign out successful");
		assertThat(user.client().hasCookie("taskly_token")).isFalse();
		assertThat(user.client().get("/api/v1/users").status()).isEqualTo(401);
	}

	@Test
	void guardsProtectedRoutes() {
		ApiResponse missing = client().get("/api/v1/users");
		ApiResponse invalid = client().get("/api/v1/users", Map.of("Cookie", "taskly_token=garbage"));

		assertThat(missing.status()).isEqualTo(401);
		assertThat((String) missing.json("$.message")).isEqualTo("Unauthorized: Token not provided");
		assertThat(invalid.status()).isEqualTo(403);
		assertThat((String) invalid.json("$.message")).isEqualTo("Forbidden: Invalid token");

		ApiClient anonymous = client();
		assertThat(anonymous.get("/api/v1/users/" + MISSING_ID).status()).isEqualTo(401);
		assertThat(anonymous.patch("/api/v1/users/update/" + MISSING_ID, "{}").status()).isEqualTo(401);
		assertThat(anonymous.delete("/api/v1/users/delete/" + MISSING_ID).status()).isEqualTo(401);
		assertThat(anonymous.emptyMultipart("/api/v1/users/upload/" + MISSING_ID).status()).isEqualTo(401);
	}

	@Test
	void listsAndFiltersUsers() {
		Registered user = register();
		String needle = user.username().substring(5).toUpperCase();

		ApiResponse all = user.client().get("/api/v1/users");
		ApiResponse filtered = user.client().get("/api/v1/users?username=" + needle + "&email=" + user.email());
		ApiResponse none = user.client().get("/api/v1/users?username=no_such_user_xyz");
		ApiResponse repeated = user.client().get("/api/v1/users?username=a&username=b");
		ApiResponse regex = user.client().get("/api/v1/users?username=.*");

		assertThat(all.status()).isEqualTo(200);
		assertThat(all.body()).doesNotContain("password");
		assertThat((Integer) filtered.json("$.length()")).isEqualTo(1);
		assertThat((String) filtered.json("$[0].username")).isEqualTo(user.username());
		assertThat(none.status()).isEqualTo(404);
		assertThat((String) none.json("$.message")).isEqualTo("No users found");
		assertThat(repeated.status()).isEqualTo(400);
		assertThat(regex.status()).isEqualTo(404);
	}

	@Test
	void getsAUserById() {
		Registered user = register();

		ApiResponse found = user.client().get("/api/v1/users/" + user.id());
		ApiResponse malformed = user.client().get("/api/v1/users/not-an-id");
		ApiResponse unknown = user.client().get("/api/v1/users/" + MISSING_ID);

		assertThat(found.status()).isEqualTo(200);
		assertThat((String) found.json("$._id")).isEqualTo(user.id());
		assertThat(malformed.status()).isEqualTo(400);
		assertThat((String) malformed.json("$.message")).isEqualTo("Invalid user ID format");
		assertThat(unknown.status()).isEqualTo(404);
		assertThat((String) unknown.json("$.message")).isEqualTo("User not found!");
	}

	@Test
	void updatesTheProfileAndPassword() {
		Registered user = register();
		String renamed = "renamed_" + System.nanoTime();

		ApiResponse response = user.client().patch("/api/v1/users/update/" + user.id(),
				ApiClient.json("username", renamed, "password", "BrandNewPassword1", "avatar", "hacked.jpg"));

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.username")).isEqualTo(renamed);
		assertThat((String) response.json("$.avatar")).isEqualTo("/images/users/default-avatar.jpg");
		ApiClient fresh = client();
		assertThat(fresh.post("/api/v1/auth/signin", ApiClient.json("email", user.email(), "password", user.password()))
				.status()).isEqualTo(401);
		assertThat(fresh.post("/api/v1/auth/signin", ApiClient.json("email", user.email(), "password", "BrandNewPassword1"))
				.status()).isEqualTo(200);
	}

	@Test
	void updateRejectsOtherAccountsCollisionsAndEmptyChanges() {
		Registered victim = register();
		Registered attacker = register();

		ApiResponse foreign = attacker.client().patch("/api/v1/users/update/" + victim.id(),
				ApiClient.json("username", "hijacked_name"));
		ApiResponse collision = attacker.client().patch("/api/v1/users/update/" + attacker.id(),
				ApiClient.json("username", victim.username()));
		ApiResponse empty = attacker.client().patch("/api/v1/users/update/" + attacker.id(), "{}");

		assertThat(foreign.status()).isEqualTo(401);
		assertThat((String) foreign.json("$.message")).isEqualTo("You can only update your own account");
		assertThat(collision.status()).isEqualTo(422);
		assertThat(empty.status()).isEqualTo(400);
	}

	@Test
	void uploadsServesAndReplacesTheAvatar() {
		Registered user = register();
		long before = avatarFileCount();

		ApiResponse first = user.client().upload("/api/v1/users/upload/" + user.id(), "avatar", "me.png", "image/png",
				PNG);

		assertThat(first.status()).isEqualTo(200);
		String firstAvatar = first.json("$.avatar");
		assertThat(firstAvatar).matches("^/images/users/avatar-[\\w-]+\\.png$");
		assertThat(avatarFileCount()).isEqualTo(before + 1);
		ApiResponse served = client().get(firstAvatar);
		assertThat(served.status()).isEqualTo(200);
		assertThat(served.header("content-type")).isEqualTo("image/png");
		assertThat(served.header("x-content-type-options")).isEqualTo("nosniff");

		ApiResponse second = user.client().upload("/api/v1/users/upload/" + user.id(), "avatar", "next.jpg",
				"image/jpeg", PNG);

		assertThat(second.status()).isEqualTo(200);
		assertThat((String) second.json("$.avatar")).endsWith(".jpg");
		assertThat(avatarFileCount()).isEqualTo(before + 1);
		assertThat(client().get(firstAvatar).status()).isEqualTo(404);
	}

	@Test
	void uploadRejectsInvalidRequests() {
		Registered user = register();
		Registered other = register();
		long before = avatarFileCount();
		String path = "/api/v1/users/upload/" + user.id();

		ApiResponse notImage = user.client().upload(path, "avatar", "xss.html", "text/html", PNG);
		ApiResponse tooLarge = user.client().upload(path, "avatar", "big.png", "image/png",
				new byte[2 * 1024 * 1024 + 1]);
		ApiResponse noFile = user.client().emptyMultipart(path);
		ApiResponse wrongField = user.client().upload(path, "photo", "me.png", "image/png", PNG);
		ApiResponse foreign = other.client().upload(path, "avatar", "me.png", "image/png", PNG);

		assertThat(notImage.status()).isEqualTo(400);
		assertThat((String) notImage.json("$.message")).contains("Only JPEG, PNG, WEBP and GIF");
		assertThat(tooLarge.status()).isEqualTo(400);
		assertThat((String) tooLarge.json("$.message")).isEqualTo("File size exceeds 2MB");
		assertThat(noFile.status()).isEqualTo(400);
		assertThat((String) noFile.json("$.message")).isEqualTo("No file uploaded");
		assertThat(wrongField.status()).isEqualTo(400);
		assertThat(foreign.status()).isEqualTo(401);
		assertThat(avatarFileCount()).isEqualTo(before);
	}

	@Test
	void deletesTheAccountEndsTheSessionAndRemovesTheAvatar() {
		Registered user = register();
		user.client().upload("/api/v1/users/upload/" + user.id(), "avatar", "me.png", "image/png", PNG);
		long before = avatarFileCount();

		ApiResponse response = user.client().delete("/api/v1/users/delete/" + user.id());

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.message")).isEqualTo("User has been deleted");
		assertThat(user.client().hasCookie("taskly_token")).isFalse();
		assertThat(avatarFileCount()).isEqualTo(before - 1);
		assertThat(client().post("/api/v1/auth/signin", ApiClient.json("email", user.email(), "password", user.password()))
				.status()).isEqualTo(404);
	}

	@Test
	void rejectsDeletingAnotherAccount() {
		Registered victim = register();
		Registered attacker = register();

		ApiResponse response = attacker.client().delete("/api/v1/users/delete/" + victim.id());

		assertThat(response.status()).isEqualTo(401);
		assertThat((String) response.json("$.message")).isEqualTo("You can only delete your own account");
		assertThat(client().post("/api/v1/auth/signin", ApiClient.json("email", victim.email(), "password", victim.password()))
				.status()).isEqualTo(200);
	}
}
