package com.todoapp.service;

import com.todoapp.dto.*;
import com.todoapp.exception.BadRequestException;
import com.todoapp.exception.ResourceNotFoundException;
import com.todoapp.exception.UnauthorizedException;
import com.todoapp.model.TaskReport;
import com.todoapp.model.Todo;
import com.todoapp.model.User;
import com.todoapp.repository.TaskReportRepository;
import com.todoapp.repository.TodoRepository;
import com.todoapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskAssignmentService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TaskReportRepository taskReportRepository;

    public TaskAssignmentService(
            TodoRepository todoRepository,
            UserRepository userRepository,
            TaskReportRepository taskReportRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
        this.taskReportRepository = taskReportRepository;
    }

    @Transactional
    public TodoDto assignTask(AssignTaskRequest request, UUID assignerId) {
        User assigner = userRepository.findById(assignerId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigner not found"));

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));

        // Validate role-based assignment permissions
        validateAssignmentPermission(assigner, assignee);

        Todo todo = Todo.builder()
                .user(assignee)
                .assignedBy(assigner)
                .assignedTo(assignee)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .dueDate(request.getDueDate())
                .originalDueDate(request.getDueDate())
                .build();

        // Set priority if provided
        if (StringUtils.hasText(request.getPriority())) {
            try {
                todo.setPriority(Todo.Priority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid priority: " + request.getPriority());
            }
        }

        todo = todoRepository.save(todo);
        return TodoDto.fromEntity(todo);
    }

    private void validateAssignmentPermission(User assigner, User assignee) {
        User.Role assignerRole = assigner.getRole();
        User.Role assigneeRole = assignee.getRole();

        // ADMIN can assign to anyone
        if (assignerRole == User.Role.ADMIN) {
            return;
        }

        // MANAGER can only assign to USER
        if (assignerRole == User.Role.MANAGER) {
            if (assigneeRole != User.Role.USER) {
                throw new UnauthorizedException("Managers can only assign tasks to users with USER role");
            }
            return;
        }

        // USER cannot assign tasks
        throw new UnauthorizedException("Only ADMIN and MANAGER can assign tasks");
    }

    @Transactional(readOnly = true)
    public List<TodoDto> getAssignedTasks(UUID userId) {
        return todoRepository.findByAssignedToIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TodoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TodoDto> getTasksAssignedByMe(UUID userId) {
        return todoRepository.findByAssignedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TodoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskReportDto submitProgressReport(UUID todoId, TaskReportRequest request, UUID reporterId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify reporter is the assignee
        if (todo.getAssignedTo() == null || !todo.getAssignedTo().getId().equals(reporterId)) {
            throw new UnauthorizedException("You can only submit reports for tasks assigned to you");
        }

        TaskReport.ReportType reportType;
        try {
            reportType = TaskReport.ReportType.valueOf(request.getReportType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid report type: " + request.getReportType());
        }

        TaskReport report = TaskReport.builder()
                .todo(todo)
                .reporter(reporter)
                .reportType(reportType)
                .message(request.getMessage())
                .build();

        // If it's a completion report, update task status
        if (reportType == TaskReport.ReportType.COMPLETION_REPORT) {
            todo.setStatus(Todo.Status.DONE);
            todoRepository.save(todo);
        }

        report = taskReportRepository.save(report);
        return TaskReportDto.fromEntity(report);
    }

    @Transactional
    public TaskReportDto provideFeedback(UUID reportId, FeedbackRequest request, UUID feedbackProviderId) {
        TaskReport report = taskReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User feedbackProvider = userRepository.findById(feedbackProviderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify feedback provider is the one who assigned the task
        if (report.getTodo().getAssignedBy() == null ||
                !report.getTodo().getAssignedBy().getId().equals(feedbackProviderId)) {
            throw new UnauthorizedException("Only the task assigner can provide feedback");
        }

        report.setFeedbackMessage(request.getFeedbackMessage());
        report.setFeedbackBy(feedbackProvider);
        report.setFeedbackAt(OffsetDateTime.now());

        report = taskReportRepository.save(report);
        return TaskReportDto.fromEntity(report);
    }

    @Transactional
    public TodoDto extendDeadline(UUID todoId, ExtendDeadlineRequest request, UUID extenderId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User extender = userRepository.findById(extenderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify extender is the one who assigned the task
        if (todo.getAssignedBy() == null || !todo.getAssignedBy().getId().equals(extenderId)) {
            throw new UnauthorizedException("Only the task assigner can extend the deadline");
        }

        // Validate new due date is in the future
        if (request.getNewDueDate().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("New due date must be in the future");
        }

        // Store original due date if not already set
        if (todo.getOriginalDueDate() == null && todo.getDueDate() != null) {
            todo.setOriginalDueDate(todo.getDueDate());
        }

        todo.setDueDate(request.getNewDueDate());
        todo = todoRepository.save(todo);

        // Create a system report for the deadline extension
        if (StringUtils.hasText(request.getReason())) {
            TaskReport extensionReport = TaskReport.builder()
                    .todo(todo)
                    .reporter(extender)
                    .reportType(TaskReport.ReportType.PROGRESS_UPDATE)
                    .message("Deadline extended to " + request.getNewDueDate() + ". Reason: " + request.getReason())
                    .feedbackBy(extender)
                    .feedbackAt(OffsetDateTime.now())
                    .build();
            taskReportRepository.save(extensionReport);
        }

        return TodoDto.fromEntity(todo);
    }

    @Transactional(readOnly = true)
    public List<TaskReportDto> getTaskReports(UUID todoId, UUID userId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify user is either assignee or assigner
        boolean isAssignee = todo.getAssignedTo() != null && todo.getAssignedTo().getId().equals(userId);
        boolean isAssigner = todo.getAssignedBy() != null && todo.getAssignedBy().getId().equals(userId);

        if (!isAssignee && !isAssigner && user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("You can only view reports for tasks you're involved with");
        }

        return taskReportRepository.findByTodoIdOrderByCreatedAtDesc(todoId)
                .stream()
                .map(TaskReportDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAssignableUsers(UUID assignerId) {
        User assigner = userRepository.findById(assignerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<User> assignableUsers;

        if (assigner.getRole() == User.Role.ADMIN) {
            // ADMIN can assign to MANAGER and USER
            assignableUsers = userRepository.findByRoleIn(
                    List.of(User.Role.MANAGER, User.Role.USER)
            );
        } else if (assigner.getRole() == User.Role.MANAGER) {
            // MANAGER can only assign to USER
            assignableUsers = userRepository.findByRole(User.Role.USER);
        } else {
            throw new UnauthorizedException("Only ADMIN and MANAGER can assign tasks");
        }

        return assignableUsers.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }
}
