import { api } from "./client";

export const getUsers = async () => {
  const res = await api.get("/api/users");
  return res.data;
};

export const createUser = async (user: any) => {
  const res = await api.post("/api/users", user);
  return res.data;
};