package com.todoapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTaskRequest {

    @NotNull(message = "Assignee ID is required")
    private UUID assigneeId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String category;

    private String priority;

    private OffsetDateTime dueDate;
}
