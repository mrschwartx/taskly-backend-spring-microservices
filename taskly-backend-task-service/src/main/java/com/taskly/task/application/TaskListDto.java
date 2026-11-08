package com.taskly.task.application;

import java.util.List;

public record TaskListDto(List<TaskDto> tasks, long taskCount) {
}
