import React, { useEffect, useState } from "react";
import { userService } from "../services/user.service";

type User = { id: number; name: string; role: string; stars: number };

const App = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [name, setName] = useState("");
  const [role, setRole] = useState("Employee");
  const [stars, setStars] = useState(0);

  useEffect(() => {
    (async () => {
      try {
        const data = await userService.getAll();
        setUsers(data);
      } catch (e) {
        console.error(e);
      }
    })();
  }, []);

  const add = async () => {
    try {
      const u = await userService.create({ name, role, stars });
      setUsers((s) => [...s, u]);
      setName("");
      setStars(0);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>Users (prototype)</h2>
      <ul>
        {users.map((u) => (
          <li key={u.id}>
            {u.name} — {u.role} — ⭐ {u.stars}
          </li>
        ))}
      </ul>

      <h3>Create user</h3>
      <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
        <input
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          placeholder="Role"
          value={role}
          onChange={(e) => setRole(e.target.value)}
        />
        <input
          type="number"
          placeholder="Stars"
          value={stars}
          onChange={(e) => setStars(Number(e.target.value))}
        />
        <button onClick={add}>Create</button>
      </div>
    </div>
  );
};

export default App;
