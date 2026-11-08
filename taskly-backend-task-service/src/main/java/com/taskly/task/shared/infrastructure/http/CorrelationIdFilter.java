package com.taskly.task.shared.infrastructure.http;

import com.taskly.task.shared.observability.CorrelationContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CorrelationIdFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String incoming = request.getHeader(CorrelationContext.HEADER);
		String correlationId = CorrelationContext.isValid(incoming) ? incoming : UUID.randomUUID().toString();
		MDC.put(CorrelationContext.MDC_KEY, correlationId);
		response.setHeader(CorrelationContext.HEADER, correlationId);
		try {
			chain.doFilter(request, response);
		}
		finally {
			MDC.remove(CorrelationContext.MDC_KEY);
		}
	}
}
