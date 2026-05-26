import { api } from "./client";

export async function register(email: string, password: string, name: string) {
  const res = await api.post("/api/auth/register", {
    email,
    password,
    name,
    role: "User",
  });

  return res.data;
}

export async function login(email: string, password: string) {
  const res = await api.post("/api/auth/login", {
    email,
    password,
  });

  return res.data;
}
