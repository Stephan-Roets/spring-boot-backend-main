package com.todoapp.repository;

import com.todoapp.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {
    List<Todo> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Todo> findAllByOrderByCreatedAtDesc();
    List<Todo> findByAssignedToIdOrderByCreatedAtDesc(UUID assignedToId);
    List<Todo> findByAssignedByIdOrderByCreatedAtDesc(UUID assignedById);
    long countByUserId(UUID userId);
    long countByUserIdAndStatus(UUID userId, Todo.Status status);
    long countByAssignedToId(UUID assignedToId);
    long countByAssignedToIdAndStatus(UUID assignedToId, Todo.Status status);
}
