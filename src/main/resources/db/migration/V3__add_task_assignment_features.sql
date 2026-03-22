-- Add task assignment columns to todos table
ALTER TABLE todos
ADD COLUMN assigned_by_id UUID REFERENCES users(id) ON DELETE SET NULL,
ADD COLUMN assigned_to_id UUID REFERENCES users(id) ON DELETE SET NULL,
ADD COLUMN original_due_date TIMESTAMPTZ;

-- Create index for assigned_by queries
CREATE INDEX idx_todos_assigned_by_id ON todos(assigned_by_id);

-- Create index for assigned_to queries
CREATE INDEX idx_todos_assigned_to_id ON todos(assigned_to_id);

-- Create task_reports table for progress updates and feedback
CREATE TABLE task_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    todo_id UUID NOT NULL REFERENCES todos(id) ON DELETE CASCADE,
    reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    report_type VARCHAR(50) NOT NULL CHECK (report_type IN ('PROGRESS_UPDATE', 'COMPLETION_REPORT', 'ISSUE_REPORT')),
    message TEXT NOT NULL,
    feedback_message TEXT,
    feedback_by_id UUID REFERENCES users(id) ON DELETE SET NULL,
    feedback_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for task_reports
CREATE INDEX idx_task_reports_todo_id ON task_reports(todo_id);
CREATE INDEX idx_task_reports_reporter_id ON task_reports(reporter_id);
CREATE INDEX idx_task_reports_feedback_by_id ON task_reports(feedback_by_id);

-- Add comments for documentation
COMMENT ON COLUMN todos.assigned_by_id IS 'User who assigned this task (ADMIN or MANAGER)';
COMMENT ON COLUMN todos.assigned_to_id IS 'User to whom this task is assigned';
COMMENT ON COLUMN todos.original_due_date IS 'Original due date before any extensions';
COMMENT ON TABLE task_reports IS 'Progress updates, completion reports, and feedback for assigned tasks';
