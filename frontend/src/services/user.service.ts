import api from "./api";

export const userService = {
  getAll: async () => {
    const res = await api.get("/users");
    return res.data;
  },
  getById: async (id: number) => {
    const res = await api.get(`/users/${id}`);
    return res.data;
  },
  create: async (user: { name: string; role: string; stars: number }) => {
    const res = await api.post(`/users`, user);
    return res.data;
  },
};
