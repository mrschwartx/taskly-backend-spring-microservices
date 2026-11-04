package com.taskly.backend.users.presentation;

import com.taskly.backend.shared.application.AuthenticatedUser;
import com.taskly.backend.shared.infrastructure.http.QueryParameters;
import com.taskly.backend.shared.infrastructure.http.RequestBodies;
import com.taskly.backend.users.application.AvatarUpload;
import com.taskly.backend.users.application.ChangeUserAvatar;
import com.taskly.backend.users.application.DeleteUser;
import com.taskly.backend.users.application.GetUser;
import com.taskly.backend.users.application.ListUsers;
import com.taskly.backend.users.application.UpdateUser;
import com.taskly.backend.users.application.UserDto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

	private final GetUser getUser;

	private final ListUsers listUsers;

	private final UpdateUser updateUser;

	private final DeleteUser deleteUser;

	private final ChangeUserAvatar changeUserAvatar;

	private final AuthCookie authCookie;

	public UsersController(GetUser getUser, ListUsers listUsers, UpdateUser updateUser, DeleteUser deleteUser,
			ChangeUserAvatar changeUserAvatar, AuthCookie authCookie) {
		this.getUser = getUser;
		this.listUsers = listUsers;
		this.updateUser = updateUser;
		this.deleteUser = deleteUser;
		this.changeUserAvatar = changeUserAvatar;
		this.authCookie = authCookie;
	}

	@GetMapping({ "", "/" })
	public List<UserDto> list(@RequestParam MultiValueMap<String, String> parameters) {
		return listUsers.execute(QueryParameters.value(parameters, "username"),
				QueryParameters.value(parameters, "email"));
	}

	@GetMapping("/{id}")
	public UserDto get(@PathVariable String id) {
		return getUser.execute(id);
	}

	@PatchMapping("/update/{id}")
	public UserDto update(@AuthenticationPrincipal AuthenticatedUser requester, @PathVariable String id,
			@RequestBody(required = false) Map<String, Object> body) {
		return updateUser.execute(requester.userId(), id, RequestBodies.orEmpty(body));
	}

	@PostMapping("/upload/{id}")
	public UserDto uploadAvatar(@AuthenticationPrincipal AuthenticatedUser requester, @PathVariable String id,
			@RequestParam(name = "avatar", required = false) MultipartFile avatar) {
		return changeUserAvatar.execute(requester.userId(), id, toUpload(avatar));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Map<String, String>> delete(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String id) {
		deleteUser.execute(requester.userId(), id);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, authCookie.clear().toString())
				.body(Map.of("message", "User has been deleted"));
	}

	private static AvatarUpload toUpload(MultipartFile file) {
		if (file == null) {
			return null;
		}
		try {
			return new AvatarUpload(file.getContentType(), file.getBytes());
		}
		catch (IOException error) {
			throw new UncheckedIOException("Failed to read uploaded avatar", error);
		}
	}
}
