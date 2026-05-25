import { env } from "../config/env";

// If VITE_API_URL is set use that, otherwise build relative URLs (e.g. `/api/...`).
const BASE_URL = env.apiBaseUrl?.replace(/\/$/, "") || "";
const buildUrl = (path: string) => {
  const p = path.startsWith("/") ? path : `/${path}`;
  // If BASE_URL already prefixed in the path, return path as-is to avoid duplication
  if (BASE_URL && p.startsWith(BASE_URL)) return p;
  return `${BASE_URL}${p}`;
};

export async function register(email: string, password: string, name: string) {
  const res = await fetch(buildUrl("/api/auth/register"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password, name, role: "User" }),
  });

  return res.json();
}

export async function login(email: string, password: string) {
  const res = await fetch(buildUrl("/api/auth/login"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  return res.json(); // returns token + user
}
