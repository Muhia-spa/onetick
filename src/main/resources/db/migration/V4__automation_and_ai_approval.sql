CREATE TABLE automation_rules (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    name VARCHAR(120) NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    condition_status VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE ai_action_proposals (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    proposed_by_user_id BIGINT NOT NULL REFERENCES users(id),
    reviewed_by_user_id BIGINT REFERENCES users(id),
    action_type VARCHAR(50) NOT NULL,
    payload VARCHAR(4000),
    status VARCHAR(30) NOT NULL,
    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    reason VARCHAR(1000),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    executed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE INDEX idx_automation_rules_workspace ON automation_rules (workspace_id);
CREATE INDEX idx_automation_rules_active ON automation_rules (active);
CREATE INDEX idx_ai_action_proposals_workspace ON ai_action_proposals (workspace_id);
CREATE INDEX idx_ai_action_proposals_status ON ai_action_proposals (status);
