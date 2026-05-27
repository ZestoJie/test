import axios from "axios";
import { api } from "./client";

interface ErrorResponse {
  message?: string;
  error?: string;
}

export interface User {
  email?: string;
  name?: string;
  [key: string]: unknown;
}

export type ApiError = {
  status: number;
  error: string;
};

export type RegisterSuccess = {
  status: number;
  user?: User;
};

export type LoginSuccess = {
  status: number;
  token?: string;
  user?: User;
};

export type RegisterResult = RegisterSuccess | ApiError;
export type LoginResult = LoginSuccess | ApiError;

function parseAxiosError(err: unknown, fallbackMessage: string): ApiError {
  if (axios.isAxiosError(err)) {
    const responseData = err.response?.data as ErrorResponse | undefined;

    return {
      status: err.response?.status ?? 500,
      error: responseData?.message ?? responseData?.error ?? fallbackMessage,
    };
  }

  return {
    status: 500,
    error: fallbackMessage,
  };
}

export async function register(
  email: string,
  password: string,
  name: string,
): Promise<RegisterResult> {
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
  } catch (err: unknown) {
    return parseAxiosError(err, "Registration failed");
  }
}

export async function login(login: string, password: string): Promise<LoginResult> {
  try {
    const res = await api.post("/api/auth/login", {
      login,
      password,
    });

    return {
      status: res.status,
      token: res.data?.token,
      user: res.data?.user,
    };
  } catch (err: unknown) {
    return parseAxiosError(err, "Login failed");
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
  } catch (err: unknown) {
    return parseAxiosError(err, "Dashboard fetch failed");
  }
}
