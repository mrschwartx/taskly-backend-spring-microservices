package com.taskly.user.shared.observability;

import java.util.regex.Pattern;
import org.slf4j.MDC;

public final class CorrelationContext {

	public static final String HEADER = "X-Correlation-Id";

	public static final String MDC_KEY = "correlationId";

	private static final Pattern VALID = Pattern.compile("^[A-Za-z0-9._-]{8,100}$");

	private CorrelationContext() {
	}

	public static boolean isValid(String value) {
		return value != null && VALID.matcher(value).matches();
	}

	public static String current() {
		return MDC.get(MDC_KEY);
	}
}
