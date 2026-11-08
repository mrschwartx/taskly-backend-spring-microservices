package com.taskly.task.domain;

import java.util.List;

public record TaskPage(List<Task> tasks, long total) {
}
