import { useState } from "react";
import { login as loginApi, showToast } from "../../api/auth";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const navigate = useNavigate();
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    if (loading) return;

    setLoading(true);

    try {
      const res = await loginApi(login, password);

      if (!res) {
        showToast("Login failed", "error");
        return;
      }

      if ("error" in res) {
        showToast(res.error, "error");
        return;
      }

      if (!res.token) {
        showToast("Invalid credentials", "error");
        return;
      }

      localStorage.setItem("token", res.token);
      localStorage.setItem("user", JSON.stringify(res.user));

      showToast("Login successful!", "info");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>Login</h2>

      <input placeholder="Email or username" onChange={(e) => setLogin(e.target.value)} />
      <input
        placeholder="password"
        type="password"
        onChange={(e) => setPassword(e.target.value)}
      />
      <button onClick={handleLogin} disabled={loading}>
        {loading ? "Logging in..." : "Login"}
      </button>
      <div>
        <button onClick={() => navigate("/")}>Home</button>
      </div>
    </div>
  );
}
