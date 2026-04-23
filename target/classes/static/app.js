async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        ...options
    });
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Request failed");
    }
    const type = response.headers.get("content-type") || "";
    if (type.includes("application/json")) {
        return response.json();
    }
    return response.text();
}

async function getSessionUser() {
    try {
        const response = await fetch("/api/auth/me", { credentials: "same-origin" });
        if (!response.ok) return null;
        return await response.json();
    } catch (_) {
        return null;
    }
}

function setStoredUser(user) {
    if (user && user.id != null) {
        localStorage.setItem("userId", String(user.id));
        localStorage.setItem("username", user.username || "");
        return;
    }
    localStorage.removeItem("userId");
    localStorage.removeItem("username");
}

function renderTopbarAuth(user) {
    const topbars = document.querySelectorAll(".topbar");
    topbars.forEach((topbar) => {
        let actionRow = topbar.querySelector(".btn-row");
        if (!actionRow) {
            actionRow = document.createElement("div");
            actionRow.className = "btn-row";
            topbar.appendChild(actionRow);
        }
        if (user) {
            actionRow.innerHTML = `
                <a class="btn-link" href="/hall.html">Hall of Mysteries</a>
                <a class="btn-link" href="/profile.html">${user.username || "Profile"}</a>
                <button type="button" class="btn-link js-logout-btn">Extinguish</button>
            `;
        } else {
            actionRow.innerHTML = `
                <a class="btn-link" href="/login.html">Sign In</a>
                <a class="btn-link" href="/register.html">Take the Case</a>
            `;
        }
    });
}

async function initAuthUI() {
    const sessionUser = await getSessionUser();
    setStoredUser(sessionUser);
    renderTopbarAuth(sessionUser);

    document.querySelectorAll(".js-logout-btn").forEach((btn) => {
        btn.onclick = async () => {
            try {
                await api("/api/auth/logout", { method: "POST" });
            } catch (_) {
                // no-op
            }
            setStoredUser(null);
            window.location.href = "/";
        };
    });
}

document.addEventListener("DOMContentLoaded", () => {
    initAuthUI();
});
