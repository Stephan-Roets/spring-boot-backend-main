package com.todoapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "feedback_message", columnDefinition = "TEXT")
    private String feedbackMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_by_id")
    private User feedbackBy;

    @Column(name = "feedback_at")
    private OffsetDateTime feedbackAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum ReportType {
        PROGRESS_UPDATE,
        COMPLETION_REPORT,
        ISSUE_REPORT
    }
}
