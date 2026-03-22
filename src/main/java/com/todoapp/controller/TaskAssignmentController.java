package com.todoapp.controller;

import com.todoapp.dto.*;
import com.todoapp.service.TaskAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    public TaskAssignmentController(TaskAssignmentService taskAssignmentService) {
        this.taskAssignmentService = taskAssignmentService;
    }

    @PostMapping("/assign")
    public ResponseEntity<TodoDto> assignTask(
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {
        UUID assignerId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskAssignmentService.assignTask(request, assignerId));
    }

    @GetMapping("/assigned-to-me")
    public ResponseEntity<List<TodoDto>> getAssignedTasks(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(taskAssignmentService.getAssignedTasks(userId));
    }

    @GetMapping("/assigned-by-me")
    public ResponseEntity<List<TodoDto>> getTasksAssignedByMe(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(taskAssignmentService.getTasksAssignedByMe(userId));
    }

    @PostMapping("/{todoId}/report")
    public ResponseEntity<TaskReportDto> submitProgressReport(
            @PathVariable UUID todoId,
            @Valid @RequestBody TaskReportRequest request,
            Authentication authentication) {
        UUID reporterId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskAssignmentService.submitProgressReport(todoId, request, reporterId));
    }

    @PostMapping("/reports/{reportId}/feedback")
    public ResponseEntity<TaskReportDto> provideFeedback(
            @PathVariable UUID reportId,
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication) {
        UUID feedbackProviderId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(
                taskAssignmentService.provideFeedback(reportId, request, feedbackProviderId));
    }

    @PutMapping("/{todoId}/extend-deadline")
    public ResponseEntity<TodoDto> extendDeadline(
            @PathVariable UUID todoId,
            @Valid @RequestBody ExtendDeadlineRequest request,
            Authentication authentication) {
        UUID extenderId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(taskAssignmentService.extendDeadline(todoId, request, extenderId));
    }

    @GetMapping("/{todoId}/reports")
    public ResponseEntity<List<TaskReportDto>> getTaskReports(
            @PathVariable UUID todoId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(taskAssignmentService.getTaskReports(todoId, userId));
    }

    @GetMapping("/assignable-users")
    public ResponseEntity<List<UserDto>> getAssignableUsers(Authentication authentication) {
        UUID assignerId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(taskAssignmentService.getAssignableUsers(assignerId));
    }
}
