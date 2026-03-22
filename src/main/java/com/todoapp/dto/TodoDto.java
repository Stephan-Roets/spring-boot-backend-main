package com.todoapp.dto;

import com.todoapp.model.Todo;
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
public class TodoDto {
    private UUID id;
    private UUID userId;
    private String userName;
    private UUID assignedById;
    private String assignedByName;
    private UUID assignedToId;
    private String assignedToName;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private OffsetDateTime dueDate;
    private OffsetDateTime originalDueDate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static TodoDto fromEntity(Todo todo) {
        return TodoDto.builder()
                .id(todo.getId())
                .userId(todo.getUser().getId())
                .userName(todo.getUser().getName())
                .assignedById(todo.getAssignedBy() != null ? todo.getAssignedBy().getId() : null)
                .assignedByName(todo.getAssignedBy() != null ? todo.getAssignedBy().getName() : null)
                .assignedToId(todo.getAssignedTo() != null ? todo.getAssignedTo().getId() : null)
                .assignedToName(todo.getAssignedTo() != null ? todo.getAssignedTo().getName() : null)
                .title(todo.getTitle())
                .description(todo.getDescription())
                .status(todo.getStatus().name())
                .priority(todo.getPriority().name())
                .category(todo.getCategory())
                .dueDate(todo.getDueDate())
                .originalDueDate(todo.getOriginalDueDate())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}
