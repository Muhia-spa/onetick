CREATE TABLE workspaces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    code VARCHAR(40) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

INSERT INTO workspaces (name, code, active, created_at, updated_at, version)
SELECT 'Default Workspace', 'DEFAULT', TRUE, now(), now(), 0
WHERE NOT EXISTS (SELECT 1 FROM workspaces WHERE code = 'DEFAULT');

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT,
    CONSTRAINT uk_projects_workspace_code UNIQUE (workspace_id, code)
);

ALTER TABLE departments ADD COLUMN workspace_id BIGINT;
UPDATE departments
SET workspace_id = (SELECT id FROM workspaces WHERE code = 'DEFAULT')
WHERE workspace_id IS NULL;
ALTER TABLE departments ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE departments
    ADD CONSTRAINT fk_departments_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id);
ALTER TABLE departments DROP CONSTRAINT IF EXISTS departments_name_key;
ALTER TABLE departments DROP CONSTRAINT IF EXISTS departments_code_key;
ALTER TABLE departments
    ADD CONSTRAINT uk_departments_workspace_code UNIQUE (workspace_id, code);
ALTER TABLE departments
    ADD CONSTRAINT uk_departments_workspace_name UNIQUE (workspace_id, name);

ALTER TABLE tasks ADD COLUMN project_id BIGINT;
ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id);

CREATE INDEX idx_departments_workspace ON departments (workspace_id);
CREATE INDEX idx_projects_workspace ON projects (workspace_id);
CREATE INDEX idx_tasks_project ON tasks (project_id);
