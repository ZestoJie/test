import { api } from "./client";

let lastLoginTime = 0;
let loginInFlight = false;

const COOLDOWN_MS = 500; // 2 req/sec
let registerInFlight = false;

export async function register(email: string, password: string, name: string) {
  if (registerInFlight) {
    return { error: "Request already in progress", status: 429 };
  }

  registerInFlight = true;

  try {
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password, name, role: "User" }),
    });

    const text = await res.text();

    try {
      return JSON.parse(text);
    } catch {
      return { error: text, status: res.status };
    }
  } finally {
    registerInFlight = false;
  }
}
export async function login(email: string, password: string) {
  const now = Date.now();

  // 🚫 cooldown check
  if (now - lastLoginTime < COOLDOWN_MS) {
    return {
      error: "Please wait before trying again.",
      status: 429,
    };
  }

  // 🚫 prevent concurrent requests
  if (loginInFlight) {
    return {
      error: "Login already in progress",
      status: 429,
    };
  }

  lastLoginTime = now;
  loginInFlight = true;

  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    const text = await res.text();

    let data: any;

    try {
      data = JSON.parse(text);
    } catch {
      return { error: text, status: res.status };
    }

    if (!res.ok) {
      return {
        error: data?.message || data?.error || "Request failed",
        status: res.status,
      };
    }

    return data;
  } finally {
    loginInFlight = false;
  }
}

export function showToast(message: string, type: "error" | "info" = "error") {
  const toast = document.createElement("div");

  toast.innerText = message;

  toast.style.position = "fixed";
  toast.style.bottom = "20px";
  toast.style.right = "20px";
  toast.style.padding = "12px 16px";
  toast.style.borderRadius = "10px";
  toast.style.color = "white";
  toast.style.zIndex = "9999";
  toast.style.fontSize = "14px";
  toast.style.boxShadow = "0 4px 12px rgba(0,0,0,0.2)";

  toast.style.background = type === "error" ? "#ef4444" : "#3b82f6";

  document.body.appendChild(toast);

  setTimeout(() => toast.remove(), 2500);
}