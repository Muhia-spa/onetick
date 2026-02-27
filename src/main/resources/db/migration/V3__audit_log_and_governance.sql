CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT REFERENCES users(id),
    actor_email VARCHAR(150),
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id BIGINT,
    workspace_id BIGINT REFERENCES workspaces(id),
    request_method VARCHAR(10),
    request_path VARCHAR(255),
    correlation_id VARCHAR(100),
    details VARCHAR(4000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_workspace_id ON audit_logs (workspace_id);
CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs (actor_user_id);
