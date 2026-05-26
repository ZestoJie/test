import { useState } from "react";
import { register, showToast } from "../../api/auth";

export default function Register() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");

  const handleRegister = async () => {
    const res = await register(email, password, name);

    if (!res || res.status >= 400) {
      showToast(res?.error || "Registration failed", "error");
      return;
    }

    if (!res.user) {
      showToast("Registration failed", "error");
      return;
    }

    showToast(`Registered: ${res.user.email || res.user.name}`, "info");
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
