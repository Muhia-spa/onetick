CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    primary_department_id BIGINT REFERENCES departments(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE department_members (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT,
    UNIQUE (department_id, user_id)
);

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    deadline TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id),
    source_department_id BIGINT NOT NULL REFERENCES departments(id),
    target_department_id BIGINT NOT NULL REFERENCES departments(id),
    assigned_to_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE task_status_history (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    old_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_by_user_id BIGINT NOT NULL REFERENCES users(id),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE task_comments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    comment VARCHAR(2000) NOT NULL,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES tasks(id),
    type VARCHAR(30) NOT NULL,
    recipient_user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE INDEX idx_tasks_deadline ON tasks (deadline);
CREATE INDEX idx_tasks_status_deadline ON tasks (status, deadline);
CREATE INDEX idx_tasks_assigned_to ON tasks (assigned_to_user_id);
