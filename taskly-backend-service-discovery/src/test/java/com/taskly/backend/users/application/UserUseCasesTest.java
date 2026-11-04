package com.taskly.backend.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taskly.backend.shared.application.AuthenticatedUser;
import com.taskly.backend.shared.domain.AlreadyExistsException;
import com.taskly.backend.shared.domain.AuthenticationFailedException;
import com.taskly.backend.shared.domain.ForbiddenException;
import com.taskly.backend.shared.domain.IntegrationEvents;
import com.taskly.backend.shared.domain.NotFoundException;
import com.taskly.backend.shared.domain.ValidationException;
import com.taskly.backend.support.FakeAvatarStorage;
import com.taskly.backend.support.FakePasswordHasher;
import com.taskly.backend.support.FakeTokenService;
import com.taskly.backend.support.InMemoryUserRepository;
import com.taskly.backend.support.RecordingEventBus;
import com.taskly.backend.users.domain.Avatar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserUseCasesTest {

	private InMemoryUserRepository users;

	private FakePasswordHasher hasher;

	private FakeTokenService tokens;

	private FakeAvatarStorage avatars;

	private RecordingEventBus events;

	private RegisterUser registerUser;

	private AuthenticateUser authenticateUser;

	private UpdateUser updateUser;

	private DeleteUser deleteUser;

	private ChangeUserAvatar changeUserAvatar;

	private GetUser getUser;

	private ListUsers listUsers;

	private VerifyAccessToken verifyAccessToken;

	@BeforeEach
	void setUp() {
		users = new InMemoryUserRepository();
		hasher = new FakePasswordHasher();
		tokens = new FakeTokenService();
		avatars = new FakeAvatarStorage();
		events = new RecordingEventBus();
		registerUser = new RegisterUser(users, hasher, tokens);
		authenticateUser = new AuthenticateUser(users, hasher, tokens);
		updateUser = new UpdateUser(users, hasher);
		deleteUser = new DeleteUser(users, avatars, events);
		changeUserAvatar = new ChangeUserAvatar(users, avatars);
		getUser = new GetUser(users);
		listUsers = new ListUsers(users);
		verifyAccessToken = new VerifyAccessToken(tokens);
	}

	private static Map<String, Object> credentials(String username, String email) {
		Map<String, Object> input = new HashMap<>();
		input.put("username", username);
		input.put("email", email);
		input.put("password", "Password123!");
		return input;
	}

	private AuthResult register(String username) {
		return registerUser.execute(credentials(username, username + "@mail.com"));
	}

	@Test
	void registersAUserAndIssuesAToken() {
		AuthResult result = register("john");

		assertThat(result.user().username()).isEqualTo("john");
		assertThat(result.user().avatar()).isEqualTo(Avatar.DEFAULT_PATH);
		assertThat(result.token()).isEqualTo("token:" + result.user().id());
		assertThat(users.findById(UUID.fromString(result.user().id()))).isPresent();
	}

	@Test
	void registrationRejectsDuplicatesIgnoringCase() {
		register("john");

		assertThatThrownBy(() -> registerUser.execute(credentials("JOHN", "other@mail.com")))
				.isInstanceOf(AlreadyExistsException.class)
				.hasMessage("Email or Username is already registered.");
		assertThatThrownBy(() -> registerUser.execute(credentials("someone", "JOHN@mail.com")))
				.isInstanceOf(AlreadyExistsException.class);
	}

	@Test
	void registrationValidatesInput() {
		assertThatThrownBy(() -> registerUser.execute(Map.of())).isInstanceOf(ValidationException.class);
		Map<String, Object> weak = credentials("john", "john@mail.com");
		weak.put("password", "short");
		assertThatThrownBy(() -> registerUser.execute(weak)).isInstanceOf(ValidationException.class);
	}

	@Test
	void authenticatesWithEmailAndPassword() {
		AuthResult registered = register("john");

		AuthResult result = authenticateUser.execute(Map.of("email", " JOHN@mail.com ", "password", "Password123!"));

		assertThat(result.user().id()).isEqualTo(registered.user().id());
	}

	@Test
	void authenticationDistinguishesFailures() {
		register("john");

		assertThatThrownBy(() -> authenticateUser.execute(Map.of("email", "ghost@mail.com", "password", "x")))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("User not found!");
		assertThatThrownBy(() -> authenticateUser.execute(Map.of("email", "john@mail.com", "password", "wrong")))
				.isInstanceOf(AuthenticationFailedException.class)
				.hasMessage("Wrong password!");
		Map<String, Object> injected = new HashMap<>();
		injected.put("email", Map.of("$ne", "x"));
		injected.put("password", "Password123!");
		assertThatThrownBy(() -> authenticateUser.execute(injected))
				.isInstanceOf(ValidationException.class)
				.hasMessage("Email and password are required");
	}

	@Test
	void verifiesAccessTokens() {
		AuthResult result = register("john");

		AuthenticatedUser user = verifyAccessToken.execute(result.token());

		assertThat(user.userId()).hasToString(result.user().id());
		assertThatThrownBy(() -> verifyAccessToken.execute(null))
				.isInstanceOf(AuthenticationFailedException.class)
				.hasMessage("Unauthorized: Token not provided");
		assertThatThrownBy(() -> verifyAccessToken.execute("garbage"))
				.isInstanceOf(ForbiddenException.class)
				.hasMessage("Forbidden: Invalid token");
	}

	@Test
	void getsAndListsUsers() {
		AuthResult john = register("john");
		register("jane");

		assertThat(getUser.execute(john.user().id()).username()).isEqualTo("john");
		assertThatThrownBy(() -> getUser.execute("not-an-id")).hasMessage("Invalid user ID format");
		assertThatThrownBy(() -> getUser.execute(UUID.randomUUID().toString()))
				.isInstanceOf(NotFoundException.class);

		assertThat(listUsers.execute(null, null)).hasSize(2);
		assertThat(listUsers.execute("JOH", null)).extracting(UserDto::username).containsExactly("john");
		assertThatThrownBy(() -> listUsers.execute("zzz", null)).hasMessage("No users found");
		assertThatThrownBy(() -> listUsers.execute(java.util.List.of("a", "b"), null))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void updatesOwnProfileAndPassword() {
		AuthResult john = register("john");
		UUID id = UUID.fromString(john.user().id());

		UserDto updated = updateUser.execute(id, john.user().id(),
				Map.of("username", "johnny", "password", "BrandNewPassword1"));

		assertThat(updated.username()).isEqualTo("johnny");
		assertThat(authenticateUser.execute(Map.of("email", "john@mail.com", "password", "BrandNewPassword1")))
				.isNotNull();
	}

	@Test
	void updateEnforcesOwnershipAndUniqueness() {
		AuthResult john = register("john");
		AuthResult jane = register("jane");
		UUID johnId = UUID.fromString(john.user().id());

		assertThatThrownBy(() -> updateUser.execute(johnId, jane.user().id(), Map.of("username", "hijack")))
				.isInstanceOf(AuthenticationFailedException.class)
				.hasMessage("You can only update your own account");
		assertThatThrownBy(() -> updateUser.execute(johnId, john.user().id(), Map.of("username", "JANE")))
				.isInstanceOf(AlreadyExistsException.class);
		assertThatThrownBy(() -> updateUser.execute(johnId, john.user().id(), Map.of()))
				.isInstanceOf(ValidationException.class)
				.hasMessage("No updatable fields provided");
		assertThat(updateUser.execute(johnId, john.user().id(), Map.of("username", "john", "avatar", "x")).avatar())
				.isEqualTo(Avatar.DEFAULT_PATH);
	}

	@Test
	void deletingAUserRemovesAvatarAndPublishesAnEvent() {
		AuthResult john = register("john");
		UUID id = UUID.fromString(john.user().id());

		deleteUser.execute(id, john.user().id());

		assertThat(users.findById(id)).isEmpty();
		assertThat(avatars.removed).containsExactly(Avatar.DEFAULT_PATH);
		assertThat(events.published).hasSize(1);
		assertThat(events.published.get(0).name()).isEqualTo(IntegrationEvents.USER_DELETED);
		assertThat(events.published.get(0).payload()).containsEntry("userId", id);
	}

	@Test
	void deletingAnotherAccountIsRejected() {
		AuthResult john = register("john");
		AuthResult jane = register("jane");

		assertThatThrownBy(() -> deleteUser.execute(UUID.fromString(john.user().id()), jane.user().id()))
				.isInstanceOf(AuthenticationFailedException.class)
				.hasMessage("You can only delete your own account");
		assertThat(events.published).isEmpty();
	}

	@Test
	void changesTheAvatarAndRemovesThePreviousOne() {
		AuthResult john = register("john");
		UUID id = UUID.fromString(john.user().id());

		UserDto first = changeUserAvatar.execute(id, john.user().id(), new AvatarUpload("image/png", new byte[] { 1 }));
		UserDto second = changeUserAvatar.execute(id, john.user().id(),
				new AvatarUpload("image/jpeg", new byte[] { 2 }));

		assertThat(first.avatar()).endsWith(".png");
		assertThat(second.avatar()).endsWith(".jpg");
		assertThat(avatars.removed).containsExactly(Avatar.DEFAULT_PATH, first.avatar());
	}

	@Test
	void avatarUploadValidatesBeforeStoring() {
		AuthResult john = register("john");
		AuthResult jane = register("jane");
		UUID id = UUID.fromString(john.user().id());

		assertThatThrownBy(() -> changeUserAvatar.execute(id, john.user().id(), null)).hasMessage("No file uploaded");
		assertThatThrownBy(
				() -> changeUserAvatar.execute(id, john.user().id(), new AvatarUpload("text/html", new byte[0])))
				.hasMessage("Only JPEG, PNG, WEBP and GIF images are allowed");
		assertThatThrownBy(() -> changeUserAvatar.execute(id, jane.user().id(),
				new AvatarUpload("image/png", new byte[0]))).isInstanceOf(AuthenticationFailedException.class);
		assertThat(avatars.stored).isEmpty();
	}
}
