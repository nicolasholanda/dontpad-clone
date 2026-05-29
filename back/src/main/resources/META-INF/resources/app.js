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

    const wsUrl = (() => {
        const scheme = window.location.protocol === "https:" ? "wss" : "ws";
        return `${scheme}://${window.location.host}/ws/${encodeURIComponent(path)}`;
    })();

    const INITIAL_BACKOFF_MS = 500;
    const MAX_BACKOFF_MS = 8000;
    const SEND_DEBOUNCE_MS = 300;
    const TYPING_QUIET_MS = 1000;

    let socket = null;
    let localVersion = 0;
    let backoffMs = INITIAL_BACKOFF_MS;
    let isUserTyping = false;
    let typingTimer = null;

    function debounce(fn, ms) {
        let t;
        return (...args) => {
            clearTimeout(t);
            t = setTimeout(() => fn(...args), ms);
        };
    }

    function connect() {
        setStatus("Connecting…");
        socket = new WebSocket(wsUrl);

        socket.addEventListener("open", () => {
            backoffMs = INITIAL_BACKOFF_MS;
            setStatus("Connected");
        });

        socket.addEventListener("message", (event) => {
            let msg;
            try {
                msg = JSON.parse(event.data);
            } catch (e) {
                return;
            }
            handleMessage(msg);
        });

        socket.addEventListener("close", () => {
            setStatus("Reconnecting…", true);
            scheduleReconnect();
        });

        socket.addEventListener("error", () => {
        });
    }

    function scheduleReconnect() {
        setTimeout(connect, backoffMs);
        backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
    }

    function handleMessage(msg) {
        switch (msg.type) {
            case "init":
                editor.value = msg.content;
                localVersion = msg.version;
                setStatus("Connected");
                break;
            case "update":
                if (!isUserTyping) {
                    editor.value = msg.content;
                }
                localVersion = msg.version;
                break;
            case "error":
                setStatus(`Error: ${msg.message}`, true);
                break;
        }
    }

    const sendUpdate = debounce(() => {
        if (socket?.readyState !== WebSocket.OPEN) {
            return;
        }
        socket.send(JSON.stringify({
            type: "update",
            content: editor.value,
            baseVersion: localVersion
        }));
        setStatus("Saved");
    }, SEND_DEBOUNCE_MS);

    editor.addEventListener("input", () => {
        isUserTyping = true;
        clearTimeout(typingTimer);
        typingTimer = setTimeout(() => { isUserTyping = false; }, TYPING_QUIET_MS);
        if (socket?.readyState === WebSocket.OPEN) {
            setStatus("Saving…");
        }
        sendUpdate();
    });

    connect();
})();
