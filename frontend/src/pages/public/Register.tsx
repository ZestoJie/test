import { useState } from "react";
import { register, showToast } from "../../api/auth";

export default function Register() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRegister = async () => {
    if (loading) return;
    setLoading(true);

    try {
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
    } finally {
      setLoading(false);
    }
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

      <button onClick={handleRegister} disabled={loading}>
        {loading ? "Registering..." : "Register"}
      </button>
    </div>
  );
}
