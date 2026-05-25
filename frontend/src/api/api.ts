import { env } from "../config/env";

// If VITE_API_URL is set use that, otherwise build relative URLs (e.g. `/api/...`).
const BASE_URL = env.apiBaseUrl?.replace(/\/$/, "") || "";
const buildUrl = (path: string) => {
  const p = path.startsWith("/") ? path : `/${path}`;
  if (BASE_URL && p.startsWith(BASE_URL)) return p;
  return `${BASE_URL}${p}`;
};

export async function getUsers() {
  const res = await fetch(buildUrl("/api/users"));
  return res.json();
}

export async function createUser(user: any) {
  const res = await fetch(buildUrl("/api/users"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(user),
  });

  return res.json();
}