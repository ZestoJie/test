import { api } from "./client";
export async function register(email: string, password: string, name: string) {
  const res = await fetch("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password, name, role: "User" }),
  });

  const text = await res.text();

  try {
    return JSON.parse(text);
  } catch {
    return { error: text };
  }
}

export async function login(email: string, password: string) {
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
    // 👇 handles 429 or HTML/plain text errors
    return {
      error: text,
      status: res.status,
    };
  }

  if (!res.ok) {
    return {
      error: data?.message || data?.error || "Request failed",
      status: res.status,
    };
  }
  return data;
}
