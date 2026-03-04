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
const goalsList = document.getElementById("goals-list");
const docsList = document.getElementById("docs-list");
const channelsList = document.getElementById("channels-list");
const messagesList = document.getElementById("messages-list");
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
        showLogin("Session expired or access denied. Please log in again.");
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
        dashboardTasks.innerHTML = `<li><span class="small">No tasks yet.</span></li>`;
    }
};

const groupByStatus = (items) => {
    const buckets = {
        NEW: [],
        IN_PROGRESS: [],
        BLOCKED: [],
        DONE: []
    };
    items.forEach((t) => {
        const status = t.status || "NEW";
        if (buckets[status]) buckets[status].push(t);
    });
    return buckets;
};

const renderKanban = (items) => {
    const grouped = groupByStatus(items);
    const cols = [
        { key: "NEW", label: "To Do" },
        { key: "IN_PROGRESS", label: "In Progress" },
        { key: "BLOCKED", label: "Blocked" },
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
    const blocked = items.filter((t) => t.status === "BLOCKED").length;
    const overdue = items.filter((t) => t.deadline && new Date(t.deadline) < new Date() && t.status !== "DONE").length;
    const completion = total ? Math.round((done / total) * 100) : 0;
    return { total, done, inProgress, blocked, overdue, completion };
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
};

const loadGoals = async () => {
    const workspaceId = document.getElementById("goal-workspace-id").value.trim() || "1";
    const goals = await apiRequest(`/api/v1/goals?workspaceId=${workspaceId}`);

    goalsList.innerHTML = goals
        .map((g) => `<li><strong>${g.title}</strong><span class="small">${g.currentValue}/${g.targetValue} | ${g.status}</span></li>`)
        .join("");
    if (!goals.length) goalsList.innerHTML = `<li><span class="small">No goals yet.</span></li>`;

    const done = goals.filter((g) => g.status === "DONE").length;
    const onTrack = goals.filter((g) => g.status === "ON_TRACK").length;
    const atRisk = goals.filter((g) => g.status === "AT_RISK").length;
    const completion = goals.length ? Math.round((done / goals.length) * 100) : 0;

    renderStats(goalStats, [
        { label: "Overall Completion", value: `${completion}%` },
        { label: "Done", value: done },
        { label: "On Track", value: onTrack },
        { label: "At Risk", value: atRisk }
    ]);
};

const loadDocs = async () => {
    const workspaceId = document.getElementById("doc-workspace-id").value.trim() || "1";
    const docs = await apiRequest(`/api/v1/docs?workspaceId=${workspaceId}`);
    docsList.innerHTML = docs
        .map((d) => `<li><strong>${d.title}</strong><span class="small">Doc #${d.id} | updated ${new Date(d.updatedAt).toLocaleString()}</span></li>`)
        .join("");
    if (!docs.length) docsList.innerHTML = `<li><span class="small">No docs yet.</span></li>`;
};

const loadChannels = async () => {
    const workspaceId = document.getElementById("channel-workspace-id").value.trim() || "1";
    const channels = await apiRequest(`/api/v1/chat/channels?workspaceId=${workspaceId}`);
    channelsList.innerHTML = channels
        .map((c) => `<li><strong># ${c.name}</strong><span class="small">${c.topic || "No topic"} | id ${c.id}</span></li>`)
        .join("");
    if (!channels.length) channelsList.innerHTML = `<li><span class="small">No channels yet.</span></li>`;
};

const loadMessages = async () => {
    const channelId = document.getElementById("message-channel-id").value.trim();
    if (!channelId) {
        messagesList.innerHTML = `<li><span class="small">Enter channel ID to load messages.</span></li>`;
        return;
    }
    const messages = await apiRequest(`/api/v1/chat/messages?channelId=${channelId}`);
    messagesList.innerHTML = messages
        .map((m) => `<li><strong>${m.senderName}</strong><span class="small">${new Date(m.createdAt).toLocaleString()}</span><div>${m.message}</div></li>`)
        .join("");
    if (!messages.length) messagesList.innerHTML = `<li><span class="small">No messages yet.</span></li>`;
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
        await loadGoals();
        await loadDocs();
        await loadChannels();
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
        try {
            if (["dashboard", "projects"].includes(screen)) await loadTaskBackedScreens();
            if (screen === "goals") await loadGoals();
            if (screen === "docs") await loadDocs();
            if (screen === "chat") {
                await loadChannels();
                await loadMessages();
            }
        } catch (err) {
            // handled by apiRequest
        }
    });
});

document.getElementById("reload-dashboard").addEventListener("click", loadTaskBackedScreens);
document.getElementById("reload-projects").addEventListener("click", loadTaskBackedScreens);
document.getElementById("reload-goals").addEventListener("click", loadGoals);
document.getElementById("reload-docs").addEventListener("click", loadDocs);
document.getElementById("reload-chat").addEventListener("click", async () => {
    await loadChannels();
    await loadMessages();
});

document.getElementById("goal-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await apiRequest("/api/v1/goals", {
            method: "POST",
            body: JSON.stringify({
                workspaceId: Number(document.getElementById("goal-workspace-id").value),
                title: document.getElementById("goal-title").value.trim(),
                targetValue: Number(document.getElementById("goal-target").value)
            })
        });
        document.getElementById("goal-title").value = "";
        document.getElementById("goal-target").value = "";
        await loadGoals();
    } catch (error) {
        loginError.textContent = error.message;
    }
});

document.getElementById("doc-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await apiRequest("/api/v1/docs", {
            method: "POST",
            body: JSON.stringify({
                workspaceId: Number(document.getElementById("doc-workspace-id").value),
                title: document.getElementById("doc-title").value.trim(),
                content: document.getElementById("doc-content").value.trim()
            })
        });
        document.getElementById("doc-title").value = "";
        document.getElementById("doc-content").value = "";
        await loadDocs();
    } catch (error) {
        loginError.textContent = error.message;
    }
});

document.getElementById("channel-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await apiRequest("/api/v1/chat/channels", {
            method: "POST",
            body: JSON.stringify({
                workspaceId: Number(document.getElementById("channel-workspace-id").value),
                name: document.getElementById("channel-name").value.trim(),
                topic: document.getElementById("channel-topic").value.trim()
            })
        });
        document.getElementById("channel-name").value = "";
        document.getElementById("channel-topic").value = "";
        await loadChannels();
    } catch (error) {
        loginError.textContent = error.message;
    }
});

document.getElementById("message-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await apiRequest("/api/v1/chat/messages", {
            method: "POST",
            body: JSON.stringify({
                channelId: Number(document.getElementById("message-channel-id").value),
                message: document.getElementById("message-text").value.trim()
            })
        });
        document.getElementById("message-text").value = "";
        await loadMessages();
    } catch (error) {
        loginError.textContent = error.message;
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
        await loadGoals();
        await loadDocs();
        await loadChannels();
    } catch (err) {
        showLogin("Session expired. Please log in again.");
    }
};

init();
