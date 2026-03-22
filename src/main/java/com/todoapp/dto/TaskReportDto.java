package com.todoapp.dto;

import com.todoapp.model.TaskReport;
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
public class TaskReportDto {

    private UUID id;
    private UUID todoId;
    private String todoTitle;
    private UUID reporterId;
    private String reporterName;
    private String reportType;
    private String message;
    private String feedbackMessage;
    private UUID feedbackById;
    private String feedbackByName;
    private OffsetDateTime feedbackAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static TaskReportDto fromEntity(TaskReport report) {
        return TaskReportDto.builder()
                .id(report.getId())
                .todoId(report.getTodo().getId())
                .todoTitle(report.getTodo().getTitle())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getName())
                .reportType(report.getReportType().name())
                .message(report.getMessage())
                .feedbackMessage(report.getFeedbackMessage())
                .feedbackById(report.getFeedbackBy() != null ? report.getFeedbackBy().getId() : null)
                .feedbackByName(report.getFeedbackBy() != null ? report.getFeedbackBy().getName() : null)
                .feedbackAt(report.getFeedbackAt())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
