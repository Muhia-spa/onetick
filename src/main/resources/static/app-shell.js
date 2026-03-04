const TOKEN_KEY = "onetick.jwt";

const loginWrap = document.getElementById("login-wrap");
const appShell = document.getElementById("app-shell");
const loginForm = document.getElementById("login-form");
const loginError = document.getElementById("login-error");
const tokenState = { value: localStorage.getItem(TOKEN_KEY) || "" };

const sessionUser = document.getElementById("session-user");
const screenTitle = document.getElementById("screen-title");
const menuButtons = Array.from(document.querySelectorAll(".menu-btn"));
const screens = {
    dashboard: document.getElementById("screen-dashboard"),
    projects: document.getElementById("screen-projects"),
    docs: document.getElementById("screen-docs"),
    goals: document.getElementById("screen-goals"),
    chat: document.getElementById("screen-chat")
};

const dashboardTasks = document.getElementById("dashboard-tasks");
const dashboardStats = document.getElementById("dashboard-stats");
const goalStats = document.getElementById("goal-stats");
const kanbanNode = document.getElementById("kanban");

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

const setToken = (token) => {
    tokenState.value = token || "";
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
};

const showLogin = (errorMessage = "") => {
    loginWrap.classList.remove("hidden");
    appShell.classList.add("hidden");
    loginError.textContent = errorMessage;
};

const showApp = () => {
    loginWrap.classList.add("hidden");
    appShell.classList.remove("hidden");
    const payload = decodeJwt(tokenState.value);
    sessionUser.textContent = payload?.sub || "Authenticated";
};

const apiRequest = async (path, options = {}) => {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    if (tokenState.value) headers.Authorization = `Bearer ${tokenState.value}`;

    const response = await fetch(path, { ...options, headers });
    const text = await response.text();
    let data = text;
    try {
        data = text ? JSON.parse(text) : {};
    } catch (err) {
        // keep text
    }

    if (response.status === 401 || response.status === 403) {
        setToken("");
        showLogin("Session expired. Please log in again.");
        throw new Error("Unauthorized");
    }

    if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}\n${JSON.stringify(data, null, 2)}`);
    }

    return data;
};

const unwrapItems = (data) => (data && Array.isArray(data.items) ? data.items : data || []);

const renderStats = (node, stats) => {
    node.innerHTML = stats
        .map((s) => `<div class="stat"><span class="small">${s.label}</span><strong>${s.value}</strong></div>`)
        .join("");
};

const renderDashboard = (items) => {
    const top = items.slice(0, 6);
    dashboardTasks.innerHTML = top
        .map((task) => {
            const title = task.title || `Task #${task.id ?? ""}`;
            const status = task.status || "UNKNOWN";
            const priority = task.priority || "-";
            const deadline = task.deadline ? new Date(task.deadline).toLocaleString() : "-";
            return `<li><strong>${title}</strong><span class="small">${status} | ${priority} | ${deadline}</span></li>`;
        })
        .join("");

    if (!top.length) {
        dashboardTasks.innerHTML = `<li><span class="small">No tasks yet. Create one from API/Swagger or task workflow.</span></li>`;
    }
};

const groupByStatus = (items) => {
    const buckets = {
        TODO: [],
        IN_PROGRESS: [],
        REVIEW: [],
        DONE: []
    };
    items.forEach((t) => {
        const status = t.status || "TODO";
        if (buckets[status]) buckets[status].push(t);
    });
    return buckets;
};

const renderKanban = (items) => {
    const grouped = groupByStatus(items);
    const cols = [
        { key: "TODO", label: "To Do" },
        { key: "IN_PROGRESS", label: "In Progress" },
        { key: "REVIEW", label: "Review" },
        { key: "DONE", label: "Done" }
    ];
    kanbanNode.innerHTML = cols
        .map((c) => {
            const cards = (grouped[c.key] || [])
                .slice(0, 8)
                .map((t) => `<div class="task-card"><strong>${t.title || `Task #${t.id}`}</strong><div class="small">${t.priority || "-"}</div></div>`)
                .join("");
            return `<div class="col"><h4>${c.label} (${(grouped[c.key] || []).length})</h4>${cards || '<div class="small">No tasks</div>'}</div>`;
        })
        .join("");
};

const computeTaskStats = (items) => {
    const total = items.length;
    const done = items.filter((t) => t.status === "DONE").length;
    const inProgress = items.filter((t) => t.status === "IN_PROGRESS").length;
    const review = items.filter((t) => t.status === "REVIEW").length;
    const overdue = items.filter((t) => t.deadline && new Date(t.deadline) < new Date() && t.status !== "DONE").length;
    const completion = total ? Math.round((done / total) * 100) : 0;
    return { total, done, inProgress, review, overdue, completion };
};

const loadTaskBackedScreens = async () => {
    const tasksPage = await apiRequest("/api/v1/tasks?page=0&size=50");
    const items = unwrapItems(tasksPage);

    renderDashboard(items);
    renderKanban(items);

    const s = computeTaskStats(items);
    renderStats(dashboardStats, [
        { label: "Total Tasks", value: s.total },
        { label: "Done", value: s.done },
        { label: "In Progress", value: s.inProgress },
        { label: "Overdue", value: s.overdue }
    ]);
    renderStats(goalStats, [
        { label: "Overall Completion", value: `${s.completion}%` },
        { label: "Done", value: s.done },
        { label: "In Review", value: s.review },
        { label: "At Risk", value: s.overdue }
    ]);
};

const setScreen = (screen) => {
    Object.entries(screens).forEach(([key, node]) => {
        node.classList.toggle("is-active", key === screen);
    });
    menuButtons.forEach((btn) => btn.classList.toggle("is-active", btn.dataset.screen === screen));
    const titles = {
        dashboard: "Dashboard",
        projects: "Projects",
        docs: "Docs",
        goals: "Goals",
        chat: "Team Chat"
    };
    screenTitle.textContent = titles[screen] || "Dashboard";
};

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    loginError.textContent = "";
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    try {
        const data = await apiRequest("/api/v1/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        setToken(data.token || "");
        showApp();
        setScreen("dashboard");
        await loadTaskBackedScreens();
    } catch (error) {
        loginError.textContent = error.message;
    }
});

document.getElementById("logout").addEventListener("click", () => {
    setToken("");
    showLogin("");
});

menuButtons.forEach((btn) => {
    btn.addEventListener("click", async () => {
        const screen = btn.dataset.screen;
        setScreen(screen);
        if (["dashboard", "projects", "goals"].includes(screen)) {
            try {
                await loadTaskBackedScreens();
            } catch (err) {
                // handled by apiRequest session flow
            }
        }
    });
});

document.getElementById("reload-dashboard").addEventListener("click", async () => {
    try {
        await loadTaskBackedScreens();
    } catch (err) {
        // handled upstream
    }
});

document.getElementById("reload-projects").addEventListener("click", async () => {
    try {
        await loadTaskBackedScreens();
    } catch (err) {
        // handled upstream
    }
});

document.getElementById("reload-goals").addEventListener("click", async () => {
    try {
        await loadTaskBackedScreens();
    } catch (err) {
        // handled upstream
    }
});

const init = async () => {
    if (!tokenState.value) {
        showLogin("");
        return;
    }
    showApp();
    setScreen("dashboard");
    try {
        await loadTaskBackedScreens();
    } catch (err) {
        showLogin("Session expired. Please log in again.");
    }
};

init();
