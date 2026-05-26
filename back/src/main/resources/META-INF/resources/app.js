"use strict";

(() => {
    const editor = document.getElementById("editor");
    const statusEl = document.getElementById("status");

    const rawPath = window.location.pathname.replace(/^\//, "");
    const path = rawPath.split("/")[0];

    function setStatus(text, isError = false) {
        statusEl.textContent = text;
        statusEl.classList.toggle("error", isError);
    }

    if (!path) {
        setStatus("Add a path to the URL (e.g. /my-note)", true);
        editor.disabled = true;
        return;
    }

    const apiUrl = `/api/notes/${encodeURIComponent(path)}`;

    let localVersion = 0;
    let userTypedSinceLastPoll = false;

    async function load() {
        try {
            const res = await fetch(apiUrl);
            if (res.status === 404) {
                localVersion = 0;
                setStatus("Saved");
                return;
            }
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            editor.value = data.content;
            localVersion = data.version;
            setStatus("Saved");
        } catch (err) {
            setStatus("Error loading", true);
        }
    }

    async function save() {
        try {
            const res = await fetch(apiUrl, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ content: editor.value })
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            localVersion = data.version;
            setStatus("Saved");
        } catch (err) {
            setStatus("Error saving", true);
        }
    }

    function debounce(fn, ms) {
        let t;
        return (...args) => {
            clearTimeout(t);
            t = setTimeout(() => fn(...args), ms);
        };
    }

    const debouncedSave = debounce(save, 500);

    editor.addEventListener("input", () => {
        userTypedSinceLastPoll = true;
        setStatus("Saving…");
        debouncedSave();
    });

    async function poll() {
        userTypedSinceLastPoll = false;
        try {
            const res = await fetch(apiUrl);
            if (!res.ok) return;
            const data = await res.json();
            if (data.version > localVersion && !userTypedSinceLastPoll) {
                editor.value = data.content;
                localVersion = data.version;
                setStatus("Saved");
            }
        } catch (err) {
        }
    }

    load();
    setInterval(poll, 3000);
})();
