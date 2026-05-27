import { getRealtimeData, setRealtimeData } from "./realtime.service";

export const userService = {
  getAll: async () => {
    const data = await getRealtimeData("users");
    if (!data) return [];
    return Object.entries(data).map(([key, value]) => {
      const record = value as Record<string, any>;
      return {
        id: typeof record.id === "number" ? record.id : Number(key),
        name: record.name ?? "",
        role: record.role ?? "Employee",
        stars:
          typeof record.stars === "number"
            ? record.stars
            : Number(record.stars ?? 0),
      };
    });
  },
  getById: async (id: number) => {
    const data = await getRealtimeData(`users/${id}`);
    if (!data) return null;
    const record = data as Record<string, any>;
    return {
      id,
      name: record.name ?? "",
      role: record.role ?? "Employee",
      stars:
        typeof record.stars === "number"
          ? record.stars
          : Number(record.stars ?? 0),
    };
  },
  create: async (user: { name: string; role: string; stars: number }) => {
    const users = await userService.getAll();
    const nextId =
      users.reduce((max, current) => Math.max(max, current.id), 0) + 1;
    const payload = {
      id: nextId,
      name: user.name,
      role: user.role,
      stars: user.stars,
    };
    await setRealtimeData(`users/${nextId}`, payload);
    return payload;
  },
};
