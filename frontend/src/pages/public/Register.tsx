import { useState } from "react";
import { login, register } from "../../api/auth";

export default function Register() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");

  const handleRegister = async () => {
    const res = await register(email, password, name);
    alert("Registered user: " + res.email);
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
