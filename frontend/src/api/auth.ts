import { api } from "./client";

let lastLoginTime = 0;
let loginInFlight = false;
const BASE_URL =
  import.meta.env.VITE_API_URL ||
  "https://terminal71-production.up.railway.app";

const COOLDOWN_MS = 500; // 2 req/sec
let registerInFlight = false;
export async function register(email: string, password: string, name: string) {
  if (registerInFlight) {
    return {
      status: 429,
      error: "Request already in progress",
    };
  }

  registerInFlight = true;

  try {
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password, name, role: "User" }),
    });

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      return {
        status: res.status,
        error: data?.message || data?.error || "Registration failed",
      };
    }

    return {
      status: 200,
      user: data.user,
    };
  } catch {
    return {
      status: 500,
      error: "Network error",
    };
  } finally {
    registerInFlight = false;
  }
}

export async function login(email: string, password: string) {
  const now = Date.now();

  if (now - lastLoginTime < COOLDOWN_MS) {
    return {
      status: 429,
      error: "Please wait before trying again.",
    };
  }

  if (loginInFlight) {
    return {
      status: 429,
      error: "Login already in progress",
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

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      return {
        status: res.status,
        error: data?.message || data?.error || "Login failed",
      };
    }

    return {
      status: 200,
      token: data.token,
      user: data.user,
    };
  } catch {
    return {
      status: 500,
      error: "Network error",
    };
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
