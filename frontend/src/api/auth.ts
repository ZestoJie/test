import { api } from "./client";

export async function register(email: string, password: string, name: string) {
  try {
    const res = await api.post("/api/auth/register", {
      email,
      password,
      name,
      role: "User",
    });

    return res.data;
  } catch (err: any) {
    return {
      error:
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        "Registration failed",
    };
  }
}

// LOGIN
export async function login(email: string, password: string) {
  try {
    const res = await api.post("/api/auth/login", {
      email,
      password,
    });

    return res.data;
  } catch (err: any) {
    return {
      error:
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        "Login failed",
    };
  }
}
