const output = document.getElementById("output");
const tokenField = document.getElementById("token");
const TOKEN_KEY = "onetick.jwt";

const print = (title, payload) => {
    const stamp = new Date().toISOString();
    const body = typeof payload === "string" ? payload : JSON.stringify(payload, null, 2);
    output.textContent = `[${stamp}] ${title}\n${body}\n\n` + output.textContent;
};

const currentToken = () => tokenField.value.trim();

const setToken = (token) => {
    tokenField.value = token || "";
    if (token) {
        localStorage.setItem(TOKEN_KEY, token);
    } else {
        localStorage.removeItem(TOKEN_KEY);
    }
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
        // Keep plain text if not JSON.
    }

    if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}\n${JSON.stringify(data, null, 2)}`);
    }
    return data;
};

const toInstant = (localDateTime) => {
    if (!localDateTime) {
        return "";
    }
    return new Date(localDateTime).toISOString();
};

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

document.getElementById("clear-token").addEventListener("click", () => {
    setToken("");
    print("TOKEN", "Token cleared");
});

document.getElementById("clear-output").addEventListener("click", () => {
    output.textContent = "";
});

document.getElementById("load-users").addEventListener("click", async () => {
    try {
        print("USERS", await apiRequest("/api/v1/users"));
    } catch (error) {
        print("USERS FAILED", error.message);
    }
});

document.getElementById("load-departments").addEventListener("click", async () => {
    try {
        print("DEPARTMENTS", await apiRequest("/api/v1/departments"));
    } catch (error) {
        print("DEPARTMENTS FAILED", error.message);
    }
});

document.getElementById("load-tasks").addEventListener("click", async () => {
    try {
        print("TASKS", await apiRequest("/api/v1/tasks"));
    } catch (error) {
        print("TASKS FAILED", error.message);
    }
});

document.getElementById("department-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        name: document.getElementById("department-name").value.trim(),
        code: document.getElementById("department-code").value.trim()
    };
    try {
        print("DEPARTMENT CREATED", await apiRequest("/api/v1/departments", {
            method: "POST",
            body: JSON.stringify(body)
        }));
    } catch (error) {
        print("DEPARTMENT CREATE FAILED", error.message);
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
        print("USER CREATED", await apiRequest("/api/v1/users", {
            method: "POST",
            body: JSON.stringify(body)
        }));
    } catch (error) {
        print("USER CREATE FAILED", error.message);
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
        targetDepartmentId: Number(document.getElementById("task-target-dept").value)
    };
    try {
        print("TASK CREATED", await apiRequest("/api/v1/tasks", {
            method: "POST",
            body: JSON.stringify(body)
        }));
    } catch (error) {
        print("TASK CREATE FAILED", error.message);
    }
});

setToken(localStorage.getItem(TOKEN_KEY) || "");
print("READY", "Use login to obtain a token, then test read/write APIs.");
