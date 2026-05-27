import { env } from "../config/env";

const API_BASE_URL = env.apiBaseUrl?.replace(/\/$/, "") || "/api";
const FIREBASE_RTDB_BASE = `${API_BASE_URL}/api/v1/firebase/rtdb`;

async function requestJson(url: string, options: RequestInit = {}) {
  const res = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {}),
    },
  });

  const json = await res.json().catch(() => null);
  if (!res.ok) {
    const message = json?.error || res.statusText || "Request failed";
    throw new Error(message);
  }
  return json;
}

export const setRealtimeData = async (path: string, data: any) => {
  await requestJson(`${FIREBASE_RTDB_BASE}/write`, {
    method: "POST",
    body: JSON.stringify({ path, data }),
  });
};

export const getRealtimeData = async (path: string) => {
  const url = `${FIREBASE_RTDB_BASE}/read/${encodeURI(path)}`;
  const response = await requestJson(url);
  return response?.data ?? null;
};

export const updateRealtimeData = async (path: string, updates: any) => {
  await requestJson(`${FIREBASE_RTDB_BASE}/update`, {
    method: "PUT",
    body: JSON.stringify({ path, updates }),
  });
};

export const deleteRealtimeData = async (path: string) => {
  const url = `${FIREBASE_RTDB_BASE}/delete/${encodeURIComponent(path)}`;
  await requestJson(url, { method: "DELETE" });
};

export const subscribeToRealtimeData = (
  path: string,
  callback: (data: any) => void,
  errorCallback?: (error: any) => void,
) => {
  const interval = window.setInterval(async () => {
    try {
      const data = await getRealtimeData(path);
      callback(data);
    } catch (error) {
      if (errorCallback) errorCallback(error);
    }
  }, 3000);

  return () => window.clearInterval(interval);
};

export const batchRealtimeUpdate = async (updates: { [path: string]: any }) => {
  const promises = Object.entries(updates).map(([path, value]) =>
    setRealtimeData(path, value),
  );
  await Promise.all(promises);
};
