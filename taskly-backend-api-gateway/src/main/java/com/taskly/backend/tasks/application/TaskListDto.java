package com.taskly.backend.tasks.application;

import java.util.List;

public record TaskListDto(List<TaskDto> tasks, long taskCount) {
}
