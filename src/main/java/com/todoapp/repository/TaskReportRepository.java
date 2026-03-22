package com.todoapp.repository;

import com.todoapp.model.TaskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskReportRepository extends JpaRepository<TaskReport, UUID> {
    List<TaskReport> findByTodoIdOrderByCreatedAtDesc(UUID todoId);
    List<TaskReport> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);
    List<TaskReport> findByTodoAssignedByIdOrderByCreatedAtDesc(UUID assignedById);
}
