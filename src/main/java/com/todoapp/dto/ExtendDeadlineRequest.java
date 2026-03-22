package com.todoapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtendDeadlineRequest {

    @NotNull(message = "New due date is required")
    private OffsetDateTime newDueDate;

    private String reason;
}
