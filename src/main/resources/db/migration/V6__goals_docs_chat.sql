CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    title VARCHAR(160) NOT NULL,
    description VARCHAR(2000),
    target_value INTEGER NOT NULL,
    current_value INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    owner_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE workspace_docs (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id),
    updated_by_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE TABLE chat_channels (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    name VARCHAR(100) NOT NULL,
    topic VARCHAR(300),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT,
    CONSTRAINT uk_chat_channel_workspace_name UNIQUE (workspace_id, name)
);

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL REFERENCES chat_channels(id),
    sender_user_id BIGINT NOT NULL REFERENCES users(id),
    message VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE INDEX idx_goals_workspace ON goals (workspace_id);
CREATE INDEX idx_workspace_docs_workspace ON workspace_docs (workspace_id);
CREATE INDEX idx_chat_channels_workspace ON chat_channels (workspace_id);
CREATE INDEX idx_chat_messages_channel_created ON chat_messages (channel_id, created_at DESC);
