package com.taskly.backend.tasks.domain;

import java.util.List;

public record TaskPage(List<Task> tasks, long total) {
}
