package com.todoapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CreateTodoRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String status;
    private String priority;
    private String category;
    private OffsetDateTime dueDate;
}
