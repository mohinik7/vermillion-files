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
