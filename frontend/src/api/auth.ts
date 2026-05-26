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
