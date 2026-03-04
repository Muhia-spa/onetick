const output = document.getElementById("output");
const tokenField = document.getElementById("token");
const TOKEN_KEY = "onetick.jwt";

const navButtons = Array.from(document.querySelectorAll(".nav-btn"));
const views = {
    admin: document.getElementById("view-admin"),
    user: document.getElementById("view-user"),
    console: document.getElementById("view-console")
};

const statusUser = document.getElementById("status-user");
const statusRoles = document.getElementById("status-roles");
const statusToken = document.getElementById("status-token");
const roleWarning = document.getElementById("role-warning");

const print = (title, payload) => {
    const stamp = new Date().toISOString();
    const body = typeof payload === "string" ? payload : JSON.stringify(payload, null, 2);
    output.textContent = `[${stamp}] ${title}\n${body}\n\n` + output.textContent;
};

const decodeJwt = (token) => {
    try {
        const parts = token.split(".");
        if (parts.length < 2) {
            return null;
        }
        const payload = parts[1].replace(/-/g, "+").replace(/_/g, "/");
        const padded = payload.padEnd(payload.length + (4 - (payload.length % 4)) % 4, "=");
        const json = atob(padded);
        return JSON.parse(json);
    } catch (err) {
        return null;
    }
};

const setView = (viewId) => {
    Object.entries(views).forEach(([key, el]) => {
        if (key === viewId) {
            el.classList.add("is-active");
        } else {
            el.classList.remove("is-active");
        }
    });
    navButtons.forEach((btn) => {
        btn.classList.toggle("is-active", btn.dataset.view === viewId);
    });
};

const currentToken = () => tokenField.value.trim();

const setToken = (token) => {
    tokenField.value = token || "";
    if (token) {
        localStorage.setItem(TOKEN_KEY, token);
    } else {
        localStorage.removeItem(TOKEN_KEY);
    }
    updateSessionStatus();
};

const updateSessionStatus = () => {
    const token = currentToken();
    if (!token) {
        statusUser.textContent = "Not signed in";
        statusRoles.textContent = "-";
        statusToken.textContent = "Missing";
        statusToken.classList.remove("status-ok");
        roleWarning.hidden = true;
        return;
    }

    const payload = decodeJwt(token);
    const email = payload?.sub || "Unknown";
    const roles = Array.isArray(payload?.roles) ? payload.roles : [];

    statusUser.textContent = email;
    statusRoles.textContent = roles.map((r) => r.replace("ROLE_", "")).join(", ") || "-";
    statusToken.textContent = "Active";
    statusToken.classList.add("status-ok");

    const hasAdmin = roles.some((r) => r.includes("ADMIN") || r.includes("MANAGER"));
    roleWarning.hidden = hasAdmin;
};

const apiRequest = async (path, options = {}) => {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };
    const token = currentToken();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(path, { ...options, headers });
    const text = await response.text();
    let data = text;
    try {
        data = text ? JSON.parse(text) : {};
    } catch (err) {
        // leave as text
    }

    if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}\n${JSON.stringify(data, null, 2)}`);
    }
    return data;
};

const toInstant = (localDateTime) => {
    if (!localDateTime) {
        return null;
    }
    return new Date(localDateTime).toISOString();
};

const renderTable = (tbodyId, rows, columns) => {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) {
        return;
    }
    tbody.innerHTML = rows
        .map((row) => {
            const cells = columns.map((col) => {
                const value = col.format ? col.format(row[col.key], row) : row[col.key];
                return `<td>${value ?? ""}</td>`;
            });
            return `<tr>${cells.join("")}</tr>`;
        })
        .join("");
};

const unwrapItems = (data) => (data && Array.isArray(data.items) ? data.items : data || []);

navButtons.forEach((btn) => {
    btn.addEventListener("click", () => setView(btn.dataset.view));
});

const oidcLogin = document.getElementById("oidc-login");
oidcLogin.addEventListener("click", () => {
    window.location.href = "/api/v1/auth/oidc/login";
});

document.getElementById("login-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    try {
        const result = await apiRequest("/api/v1/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        setToken(result.token || "");
        print("LOGIN SUCCESS", result);
    } catch (error) {
        print("LOGIN FAILED", error.message);
    }
});

document.getElementById("logout").addEventListener("click", () => {
    setToken("");
    print("TOKEN", "Token cleared");
});

document.getElementById("copy-token").addEventListener("click", async () => {
    try {
        await navigator.clipboard.writeText(currentToken());
        print("TOKEN", "Token copied");
    } catch (error) {
        print("TOKEN COPY FAILED", error.message);
    }
});

document.getElementById("clear-output").addEventListener("click", () => {
    output.textContent = "";
});

// Admin - Workspaces

document.getElementById("load-workspaces").addEventListener("click", async () => {
    try {
        const data = await apiRequest("/api/v1/workspaces");
        renderTable("workspaces-body", data, [
            { key: "id" },
            { key: "name" },
            { key: "code" },
            { key: "active" }
        ]);
        print("WORKSPACES", data);
    } catch (error) {
        print("WORKSPACES FAILED", error.message);
    }
});

document.getElementById("workspace-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        name: document.getElementById("workspace-name").value.trim(),
        code: document.getElementById("workspace-code").value.trim()
    };
    try {
        const data = await apiRequest("/api/v1/workspaces", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("WORKSPACE CREATED", data);
    } catch (error) {
        print("WORKSPACE CREATE FAILED", error.message);
    }
});

// Admin - Projects

document.getElementById("load-projects").addEventListener("click", async () => {
    const workspaceId = document.getElementById("projects-workspace-id").value.trim();
    const query = workspaceId ? `?workspaceId=${workspaceId}` : "";
    try {
        const data = await apiRequest(`/api/v1/projects${query}`);
        renderTable("projects-body", data, [
            { key: "id" },
            { key: "name" },
            { key: "code" },
            { key: "workspaceId" }
        ]);
        print("PROJECTS", data);
    } catch (error) {
        print("PROJECTS FAILED", error.message);
    }
});

document.getElementById("project-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        workspaceId: Number(document.getElementById("project-workspace-id").value),
        name: document.getElementById("project-name").value.trim(),
        code: document.getElementById("project-code").value.trim()
    };
    try {
        const data = await apiRequest("/api/v1/projects", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("PROJECT CREATED", data);
    } catch (error) {
        print("PROJECT CREATE FAILED", error.message);
    }
});

// Admin - Departments

document.getElementById("load-departments").addEventListener("click", async () => {
    const search = document.getElementById("departments-search").value.trim();
    const workspaceId = document.getElementById("departments-workspace-id").value.trim();
    const query = new URLSearchParams({ page: "0", size: "50" });
    if (search) query.set("search", search);
    if (workspaceId) query.set("workspaceId", workspaceId);

    try {
        const data = await apiRequest(`/api/v1/departments?${query.toString()}`);
        const items = unwrapItems(data);
        renderTable("departments-body", items, [
            { key: "id" },
            { key: "name" },
            { key: "code" },
            { key: "workspaceId" }
        ]);
        print("DEPARTMENTS", data);
    } catch (error) {
        print("DEPARTMENTS FAILED", error.message);
    }
});

document.getElementById("department-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        workspaceId: Number(document.getElementById("department-workspace-id").value),
        name: document.getElementById("department-name").value.trim(),
        code: document.getElementById("department-code").value.trim()
    };
    try {
        const data = await apiRequest("/api/v1/departments", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("DEPARTMENT CREATED", data);
    } catch (error) {
        print("DEPARTMENT CREATE FAILED", error.message);
    }
});

// Admin - Users

document.getElementById("load-users").addEventListener("click", async () => {
    try {
        const data = await apiRequest("/api/v1/users");
        renderTable("users-body", data, [
            { key: "id" },
            { key: "email" },
            { key: "name" },
            { key: "roles", format: (value) => (value || []).join(", ") }
        ]);
        print("USERS", data);
    } catch (error) {
        print("USERS FAILED", error.message);
    }
});

document.getElementById("user-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const deptValue = document.getElementById("user-dept-id").value.trim();
    const body = {
        email: document.getElementById("user-email").value.trim(),
        name: document.getElementById("user-name").value.trim(),
        password: document.getElementById("user-password").value,
        primaryDepartmentId: deptValue ? Number(deptValue) : null,
        roles: [document.getElementById("user-role").value]
    };
    try {
        const data = await apiRequest("/api/v1/users", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("USER CREATED", data);
    } catch (error) {
        print("USER CREATE FAILED", error.message);
    }
});

// Admin - Tasks

document.getElementById("load-tasks").addEventListener("click", async () => {
    const status = document.getElementById("tasks-status").value;
    const assigned = document.getElementById("tasks-assigned").value.trim();
    const project = document.getElementById("tasks-project").value.trim();
    const query = new URLSearchParams({ page: "0", size: "50" });
    if (status) query.set("status", status);
    if (assigned) query.set("assignedToUserId", assigned);
    if (project) query.set("projectId", project);

    try {
        const data = await apiRequest(`/api/v1/tasks?${query.toString()}`);
        const items = unwrapItems(data);
        renderTable("tasks-body", items, [
            { key: "id" },
            { key: "title" },
            { key: "status" },
            { key: "priority" },
            { key: "assignedToUserId" }
        ]);
        print("TASKS", data);
    } catch (error) {
        print("TASKS FAILED", error.message);
    }
});

document.getElementById("assign-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        taskId: Number(document.getElementById("assign-task-id").value),
        assignedToUserId: Number(document.getElementById("assign-user-id").value)
    };
    try {
        const data = await apiRequest("/api/v1/tasks/assign", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("TASK ASSIGNED", data);
    } catch (error) {
        print("TASK ASSIGN FAILED", error.message);
    }
});

// Admin - Automation

document.getElementById("load-automation").addEventListener("click", async () => {
    const workspaceId = document.getElementById("automation-workspace-id").value.trim();
    const query = workspaceId ? `?workspaceId=${workspaceId}` : "";
    try {
        const data = await apiRequest(`/api/v1/automation/rules${query}`);
        renderTable("automation-body", data, [
            { key: "id" },
            { key: "name" },
            { key: "triggerType" },
            { key: "actionType" },
            { key: "active" }
        ]);
        print("AUTOMATION RULES", data);
    } catch (error) {
        print("AUTOMATION LOAD FAILED", error.message);
    }
});

document.getElementById("automation-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const condition = document.getElementById("automation-condition").value;
    const body = {
        workspaceId: Number(document.getElementById("automation-workspace").value),
        name: document.getElementById("automation-name").value.trim(),
        triggerType: document.getElementById("automation-trigger").value,
        actionType: document.getElementById("automation-action").value,
        conditionStatus: condition || null
    };
    try {
        const data = await apiRequest("/api/v1/automation/rules", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("AUTOMATION CREATED", data);
    } catch (error) {
        print("AUTOMATION CREATE FAILED", error.message);
    }
});

// Admin - AI proposals

document.getElementById("load-ai").addEventListener("click", async () => {
    const workspaceId = document.getElementById("ai-workspace-id").value.trim();
    const status = document.getElementById("ai-status-filter").value;
    const query = new URLSearchParams({ page: "0", size: "50" });
    if (workspaceId) query.set("workspaceId", workspaceId);
    if (status) query.set("status", status);

    try {
        const data = await apiRequest(`/api/v1/ai/actions?${query.toString()}`);
        const items = unwrapItems(data);
        renderTable("ai-body", items, [
            { key: "id" },
            { key: "taskId" },
            { key: "status" },
            { key: "approvalRequired" },
            { key: "reason" }
        ]);
        print("AI PROPOSALS", data);
    } catch (error) {
        print("AI PROPOSALS FAILED", error.message);
    }
});

document.getElementById("ai-propose-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        taskId: Number(document.getElementById("ai-task-id").value),
        targetStatus: document.getElementById("ai-target-status").value,
        reason: document.getElementById("ai-reason").value.trim()
    };
    try {
        const data = await apiRequest("/api/v1/ai/actions/status-change", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("AI PROPOSAL CREATED", data);
    } catch (error) {
        print("AI PROPOSAL FAILED", error.message);
    }
});

document.getElementById("ai-approve").addEventListener("click", async () => {
    const proposalId = document.getElementById("ai-proposal-id").value.trim();
    if (!proposalId) {
        print("AI APPROVE", "Proposal ID is required");
        return;
    }
    const body = { comment: document.getElementById("ai-comment").value.trim() };
    try {
        const data = await apiRequest(`/api/v1/ai/actions/${proposalId}/approve`, {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("AI APPROVED", data);
    } catch (error) {
        print("AI APPROVE FAILED", error.message);
    }
});

document.getElementById("ai-reject").addEventListener("click", async () => {
    const proposalId = document.getElementById("ai-proposal-id").value.trim();
    if (!proposalId) {
        print("AI REJECT", "Proposal ID is required");
        return;
    }
    const body = { comment: document.getElementById("ai-comment").value.trim() };
    try {
        const data = await apiRequest(`/api/v1/ai/actions/${proposalId}/reject`, {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("AI REJECTED", data);
    } catch (error) {
        print("AI REJECT FAILED", error.message);
    }
});

// Admin - Audit Logs

document.getElementById("load-audits").addEventListener("click", async () => {
    const workspaceId = document.getElementById("audit-workspace-id").value.trim();
    const fromValue = document.getElementById("audit-from").value;
    const toValue = document.getElementById("audit-to").value;
    const query = new URLSearchParams({ page: "0", size: "50" });
    if (workspaceId) query.set("workspaceId", workspaceId);
    if (fromValue) query.set("from", toInstant(fromValue));
    if (toValue) query.set("to", toInstant(toValue));

    try {
        const data = await apiRequest(`/api/v1/audit-logs?${query.toString()}`);
        const items = unwrapItems(data);
        renderTable("audit-body", items, [
            { key: "id" },
            { key: "action" },
            { key: "actorEmail" },
            { key: "entityType" },
            { key: "createdAt" }
        ]);
        print("AUDIT LOGS", data);
    } catch (error) {
        print("AUDIT LOAD FAILED", error.message);
    }
});

// User - My tasks

document.getElementById("load-my-tasks").addEventListener("click", async () => {
    const status = document.getElementById("my-status").value;
    const userId = document.getElementById("my-user-id").value.trim();
    const query = new URLSearchParams({ page: "0", size: "50" });
    if (status) query.set("status", status);
    if (userId) query.set("assignedToUserId", userId);

    try {
        const data = await apiRequest(`/api/v1/tasks?${query.toString()}`);
        const items = unwrapItems(data);
        renderTable("my-tasks-body", items, [
            { key: "id" },
            { key: "title" },
            { key: "status" },
            { key: "priority" },
            { key: "deadline" }
        ]);
        print("MY TASKS", data);
    } catch (error) {
        print("MY TASKS FAILED", error.message);
    }
});

document.getElementById("status-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        taskId: Number(document.getElementById("status-task-id").value),
        changedByUserId: Number(document.getElementById("status-user-id").value),
        status: document.getElementById("status-value").value
    };
    try {
        const data = await apiRequest("/api/v1/tasks/status", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("STATUS UPDATED", data);
    } catch (error) {
        print("STATUS UPDATE FAILED", error.message);
    }
});

document.getElementById("comment-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        taskId: Number(document.getElementById("comment-task-id").value),
        createdByUserId: Number(document.getElementById("comment-user-id").value),
        comment: document.getElementById("comment-text").value.trim()
    };
    try {
        const data = await apiRequest("/api/v1/tasks/comments", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("COMMENT ADDED", data);
    } catch (error) {
        print("COMMENT FAILED", error.message);
    }
});

document.getElementById("task-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        title: document.getElementById("task-title").value.trim(),
        description: document.getElementById("task-description").value.trim(),
        priority: document.getElementById("task-priority").value,
        deadline: toInstant(document.getElementById("task-deadline").value),
        createdByUserId: Number(document.getElementById("task-created-by").value),
        sourceDepartmentId: Number(document.getElementById("task-source-dept").value),
        targetDepartmentId: Number(document.getElementById("task-target-dept").value),
        projectId: document.getElementById("task-project-id").value ? Number(document.getElementById("task-project-id").value) : null
    };
    try {
        const data = await apiRequest("/api/v1/tasks", {
            method: "POST",
            body: JSON.stringify(body)
        });
        print("TASK CREATED", data);
    } catch (error) {
        print("TASK CREATE FAILED", error.message);
    }
});

// API Console

document.getElementById("api-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const method = document.getElementById("api-method").value;
    const path = document.getElementById("api-path").value.trim();
    const bodyText = document.getElementById("api-body").value.trim();

    let options = { method };
    if (bodyText) {
        try {
            options.body = JSON.stringify(JSON.parse(bodyText));
        } catch (err) {
            print("API CONSOLE", "Invalid JSON body");
            return;
        }
    }

    try {
        const data = await apiRequest(path, options);
        print("API RESPONSE", data);
    } catch (error) {
        print("API ERROR", error.message);
    }
});

setToken(localStorage.getItem(TOKEN_KEY) || "");
setView("admin");
print("READY", "Use login to obtain a token, then use admin or user flows.");
