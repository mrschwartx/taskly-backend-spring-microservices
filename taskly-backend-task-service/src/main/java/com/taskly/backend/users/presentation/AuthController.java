package com.taskly.backend.users.presentation;

import com.taskly.backend.shared.infrastructure.http.RequestBodies;
import com.taskly.backend.users.application.AuthResult;
import com.taskly.backend.users.application.AuthenticateUser;
import com.taskly.backend.users.application.RegisterUser;
import com.taskly.backend.users.application.UserDto;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final RegisterUser registerUser;

	private final AuthenticateUser authenticateUser;

	private final AuthCookie authCookie;

	public AuthController(RegisterUser registerUser, AuthenticateUser authenticateUser, AuthCookie authCookie) {
		this.registerUser = registerUser;
		this.authenticateUser = authenticateUser;
		this.authCookie = authCookie;
	}

	@PostMapping("/signup")
	public ResponseEntity<UserDto> signUp(@RequestBody(required = false) Map<String, Object> body) {
		return withSession(registerUser.execute(RequestBodies.orEmpty(body)));
	}

	@PostMapping("/signin")
	public ResponseEntity<UserDto> signIn(@RequestBody(required = false) Map<String, Object> body) {
		return withSession(authenticateUser.execute(RequestBodies.orEmpty(body)));
	}

	@GetMapping("/signout")
	public ResponseEntity<Map<String, String>> signOut() {
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, authCookie.clear().toString())
				.body(Map.of("message", "Sign out successful"));
	}

	private ResponseEntity<UserDto> withSession(AuthResult result) {
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, authCookie.issue(result.token()).toString())
				.body(result.user());
	}
}
