package com.todoapp.service;

import com.todoapp.dto.CreateTodoRequest;
import com.todoapp.dto.TodoDto;
import com.todoapp.exception.BadRequestException;
import com.todoapp.exception.ResourceNotFoundException;
import com.todoapp.exception.UnauthorizedException;
import com.todoapp.model.Todo;
import com.todoapp.model.User;
import com.todoapp.repository.TodoRepository;
import com.todoapp.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoService(
        TodoRepository todoRepository,
        UserRepository userRepository
    ) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TodoDto> getUserTodos(UUID userId) {
        return todoRepository
            .findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(TodoDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TodoDto> getAllTodos() {
        return todoRepository
            .findAllByOrderByCreatedAtDesc()
            .stream()
            .map(TodoDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TodoDto getTodoById(UUID todoId, UUID userId) {
        Todo todo = todoRepository
            .findById(todoId)
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only owner or ADMIN can view
        if (
            !todo.getUser().getId().equals(userId) &&
            user.getRole() != User.Role.ADMIN
        ) {
            throw new UnauthorizedException("You can only view your own todos");
        }

        return TodoDto.fromEntity(todo);
    }

    @Transactional
    public TodoDto createTodo(CreateTodoRequest request, UUID userId) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Todo todo = Todo.builder()
            .user(user)
            .title(request.getTitle())
            .description(request.getDescription())
            .category(request.getCategory())
            .dueDate(request.getDueDate())
            .build();

        // Set status if provided
        if (StringUtils.hasText(request.getStatus())) {
            try {
                todo.setStatus(
                    Todo.Status.valueOf(request.getStatus().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                    "Invalid status: " + request.getStatus()
                );
            }
        }

        // Set priority if provided
        if (StringUtils.hasText(request.getPriority())) {
            try {
                todo.setPriority(
                    Todo.Priority.valueOf(request.getPriority().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                    "Invalid priority: " + request.getPriority()
                );
            }
        }

        todo = todoRepository.save(todo);
        return TodoDto.fromEntity(todo);
    }

    @Transactional
    public TodoDto updateTodo(
        UUID todoId,
        CreateTodoRequest request,
        UUID userId
    ) {
        Todo todo = todoRepository
            .findById(todoId)
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only owner or ADMIN can update
        if (
            !todo.getUser().getId().equals(userId) &&
            user.getRole() != User.Role.ADMIN
        ) {
            throw new UnauthorizedException(
                "You can only update your own todos"
            );
        }

        if (StringUtils.hasText(request.getTitle())) todo.setTitle(
            request.getTitle()
        );
        if (request.getDescription() != null) todo.setDescription(
            request.getDescription()
        );
        if (request.getCategory() != null) todo.setCategory(
            request.getCategory()
        );
        if (request.getDueDate() != null) todo.setDueDate(request.getDueDate());

        if (StringUtils.hasText(request.getStatus())) {
            try {
                todo.setStatus(
                    Todo.Status.valueOf(request.getStatus().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                    "Invalid status: " + request.getStatus()
                );
            }
        }

        if (StringUtils.hasText(request.getPriority())) {
            try {
                todo.setPriority(
                    Todo.Priority.valueOf(request.getPriority().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                    "Invalid priority: " + request.getPriority()
                );
            }
        }

        todo = todoRepository.save(todo);
        return TodoDto.fromEntity(todo);
    }

    @Transactional
    public void deleteTodo(UUID todoId, UUID userId) {
        Todo todo = todoRepository
            .findById(todoId)
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only owner or ADMIN can delete
        if (
            !todo.getUser().getId().equals(userId) &&
            user.getRole() != User.Role.ADMIN
        ) {
            throw new UnauthorizedException(
                "You can only delete your own todos"
            );
        }

        todoRepository.delete(todo);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUserStats(UUID userId) {
        long total = todoRepository.countByUserId(userId);
        long pending = todoRepository.countByUserIdAndStatus(
            userId,
            Todo.Status.PENDING
        );
        long inProgress = todoRepository.countByUserIdAndStatus(
            userId,
            Todo.Status.IN_PROGRESS
        );
        long done = todoRepository.countByUserIdAndStatus(
            userId,
            Todo.Status.DONE
        );

        return Map.of(
            "total",
            total,
            "pending",
            pending,
            "inProgress",
            inProgress,
            "done",
            done
        );
    }
}
