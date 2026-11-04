package com.taskly.backend.shared.infrastructure.http;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

	@GetMapping("/")
	public Map<String, String> welcome() {
		return Map.of("message", "Welcome to the Taskly API");
	}
}
