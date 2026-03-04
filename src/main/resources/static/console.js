const tokenField = document.getElementById("token");
const logNode = document.getElementById("log");
const sessionNode = document.getElementById("session");
const TOKEN_KEY = "onetick.jwt";

const print = (title, payload) => {
    const stamp = new Date().toISOString();
    const body = typeof payload === "string" ? payload : JSON.stringify(payload, null, 2);
    logNode.textContent = `[${stamp}] ${title}\n${body}\n\n` + logNode.textContent;
};

const setToken = (token) => {
    tokenField.value = token || "";
    if (token) {
        localStorage.setItem(TOKEN_KEY, token);
    } else {
        localStorage.removeItem(TOKEN_KEY);
    }
    updateSession();
};

const decodeJwt = (token) => {
    try {
        const payload = token.split(".")[1];
        if (!payload) return null;
        const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
        const padded = normalized.padEnd(normalized.length + (4 - (normalized.length % 4)) % 4, "=");
        return JSON.parse(atob(padded));
    } catch (err) {
        return null;
    }
};

const updateSession = () => {
    const token = tokenField.value.trim();
    if (!token) {
        sessionNode.textContent = "Session: not signed in";
        return;
    }
    const payload = decodeJwt(token);
    const user = payload?.sub || "unknown";
    const roles = Array.isArray(payload?.roles) ? payload.roles.join(", ") : "-";
    sessionNode.textContent = `Session: ${user} | Roles: ${roles}`;
};

const apiRequest = async (path, options = {}) => {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    const token = tokenField.value.trim();
    if (token) headers.Authorization = `Bearer ${token}`;
    const response = await fetch(path, { ...options, headers });
    const text = await response.text();
    let data = text;
    try {
        data = text ? JSON.parse(text) : {};
    } catch (err) {
        // keep text
    }
    if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}\n${JSON.stringify(data, null, 2)}`);
    }
    return data;
};

const setOutput = (id, payload) => {
    document.getElementById(id).textContent = JSON.stringify(payload, null, 2);
};

const futureIso = (localDateTime) => {
    return new Date(localDateTime).toISOString();
};

document.getElementById("login-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    try {
        const data = await apiRequest("/api/v1/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        setToken(data.token || "");
        print("LOGIN SUCCESS", data);
    } catch (error) {
        print("LOGIN FAILED", error.message);
    }
});

document.getElementById("logout").addEventListener("click", () => {
    setToken("");
    print("LOGOUT", "Token cleared");
});

document.getElementById("clear-log").addEventListener("click", () => {
    logNode.textContent = "";
});

document.getElementById("load-workspaces").addEventListener("click", async () => {
    try {
        const data = await apiRequest("/api/v1/workspaces");
        setOutput("workspaces-out", data);
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

document.getElementById("load-projects").addEventListener("click", async () => {
    try {
        const data = await apiRequest("/api/v1/projects");
        setOutput("projects-out", data);
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

document.getElementById("load-departments").addEventListener("click", async () => {
    try {
        const data = await apiRequest("/api/v1/departments?page=0&size=50");
        setOutput("departments-out", data);
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

document.getElementById("load-users").addEventListener("click", async () => {
    try {
        const data = await apiRequest("/api/v1/users");
        setOutput("users-out", data);
    } catch (error) {
        print("USERS FAILED", error.message);
    }
});

document.getElementById("user-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const primaryDepartmentIdRaw = document.getElementById("user-department-id").value.trim();
    const body = {
        email: document.getElementById("user-email").value.trim(),
        name: document.getElementById("user-name").value.trim(),
        password: document.getElementById("user-password").value,
        primaryDepartmentId: primaryDepartmentIdRaw ? Number(primaryDepartmentIdRaw) : null,
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

document.getElementById("load-tasks").addEventListener("click", async () => {
    try {
        const data = await apiRequest("/api/v1/tasks?page=0&size=50");
        setOutput("tasks-out", data);
    } catch (error) {
        print("TASKS FAILED", error.message);
    }
});

document.getElementById("task-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const projectRaw = document.getElementById("task-project").value.trim();
    const deadline = document.getElementById("task-deadline").value;
    const body = {
        title: document.getElementById("task-title").value.trim(),
        description: document.getElementById("task-description").value.trim(),
        priority: document.getElementById("task-priority").value,
        deadline: futureIso(deadline),
        createdByUserId: Number(document.getElementById("task-created-by").value),
        sourceDepartmentId: Number(document.getElementById("task-source").value),
        targetDepartmentId: Number(document.getElementById("task-target").value),
        projectId: projectRaw ? Number(projectRaw) : null
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

setToken(localStorage.getItem(TOKEN_KEY) || "");
print("READY", "Login, then use panels to create and load data through backend APIs.");
