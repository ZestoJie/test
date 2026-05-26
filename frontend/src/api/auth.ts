import { api } from "./client";

let lastLoginTime = 0;
let loginInFlight = false;

const COOLDOWN_MS = 1000; // 1 req/sec
let registerInFlight = false;

export async function register(email: string, password: string, name: string) {
  try {
    const res = await api.post("/api/auth/register", {
      email,
      password,
      name,
      role: "User",
    });

    return {
      status: res.status,
      user: res.data?.user,
    };
  } catch (err: any) {
    return {
      status: err?.response?.status || 500,
      error:
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        "Registration failed",
    };
  }
}

export async function login(email: string, password: string) {
  try {
    const res = await api.post("/api/auth/login", {
      email,
      password,
    });

    return {
      status: res.status,
      token: res.data?.token,
      user: res.data?.user,
    };
  } catch (err: any) {
    return {
      status: err?.response?.status || 500,
      error:
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        "Login failed",
    };
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

export async function dashboard() {
  try {
    const res = await api.get("/api/dashboard");

    return {
      status: res.status,
      data: res.data,
    };
  } catch (err: any) {
    return {
      status: err?.response?.status || 500,
      error:
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        "Dashboard fetch failed",
    };
  }
}
