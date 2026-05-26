import { useState } from "react";
import { login, showToast } from "../../api/auth";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {
    const res = await login(email, password);

    // ❌ rate limit or frontend errors
    if (!res || res.status >= 400) {
      showToast(res?.error || "Login failed", "error");
      return;
    }

    // ❌ backend error response
    if (!res.token) {
      showToast("Invalid credentials", "error");
      return;
    }

    // ✅ success
    localStorage.setItem("token", res.token);
    localStorage.setItem("user", JSON.stringify(res.user));

    showToast("Login successful!", "info");
  };

  return (
    <div>
      <h2>Login</h2>

      <input placeholder="email" onChange={(e) => setEmail(e.target.value)} />
      <input
        placeholder="password"
        type="password"
        onChange={(e) => setPassword(e.target.value)}
      />

      <button onClick={handleLogin}>Login</button>
    </div>
  );
}
