package com.taskly.backend.config;

import com.taskly.backend.shared.application.AuthenticatedUser;
import com.taskly.backend.shared.domain.DomainException;
import com.taskly.backend.users.application.VerifyAccessToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final List<String> PROTECTED_PREFIXES = List.of("/api/v1/users", "/api/v1/tasks");

	private final VerifyAccessToken verifyAccessToken;

	private final String cookieName;

	private final HandlerExceptionResolver exceptionResolver;

	public JwtAuthenticationFilter(VerifyAccessToken verifyAccessToken, String cookieName,
			HandlerExceptionResolver exceptionResolver) {
		this.verifyAccessToken = verifyAccessToken;
		this.cookieName = cookieName;
		this.exceptionResolver = exceptionResolver;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return PROTECTED_PREFIXES.stream().noneMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			AuthenticatedUser user = verifyAccessToken.execute(readToken(request));
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, List.of()));
			SecurityContextHolder.setContext(context);
		}
		catch (DomainException error) {
			SecurityContextHolder.clearContext();
			exceptionResolver.resolveException(request, response, null, error);
			return;
		}
		chain.doFilter(request, response);
	}

	private String readToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
