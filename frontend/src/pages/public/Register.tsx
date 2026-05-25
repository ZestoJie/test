import { useState } from "react";
import { register } from "../../api/auth";

export default function Register() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");

  const handleRegister = async () => {
    const res = await register(email, password, name);

    if (res?.user) {
      alert(
        "Registered user: " + (res.user.email ?? res.user.name ?? "unknown"),
      );
      return;
    }

    alert("Registration failed: " + (res?.error || "Unknown error"));
  };

  return (
    <div>
      <h2>Register</h2>

      <input placeholder="name" onChange={(e) => setName(e.target.value)} />
      <input placeholder="email" onChange={(e) => setEmail(e.target.value)} />
      <input
        placeholder="password"
        type="password"
        onChange={(e) => setPassword(e.target.value)}
      />

      <button onClick={handleRegister}>Register</button>
    </div>
  );
}
export function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
}
