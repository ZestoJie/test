import { env } from "../config/env";

const BASE_URL =
  env.apiBaseUrl?.replace(/\/$/, "") ||
  "https://terminal71-production.up.railway.app";

const buildUrl = (path: string) => {
  const p = path.startsWith("/") ? path : `/${path}`;
  return `${BASE_URL}${p}`;
};

const authHeaders = () => {
  const token = localStorage.getItem("token");

  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};
export async function getUsers() {
  const res = await fetch(buildUrl("/api/users"), {
    method: "GET",
    headers: authHeaders(),
  });

  return res.json();
}
export async function createUser(user: unknown) {
  const res = await fetch(buildUrl("/api/users"), {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(user),
  });

  return res.json();
}
