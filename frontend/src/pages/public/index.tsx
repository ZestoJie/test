import React, { useEffect, type JSX } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  useNavigate,
} from "react-router-dom";

import Login from "../../pages/public/Login";
import Register from "../../pages/public/Register";
import Dashboard from "../../pages/public/Dashboard";

function Home() {
  const navigate = useNavigate();

  const token = localStorage.getItem("token");
  const rawUser = localStorage.getItem("user");

  // eslint-disable-next-line no-useless-assignment
  let user = null;

  try {
    user = rawUser ? JSON.parse(rawUser) : null;
  } catch {
    user = null;
  }

  return (
    <div style={{ padding: 20 }}>
      <h1>{token ? `Welcome back, ${user?.name ?? "user"}!` : "Welcome!"}</h1>

      {token ? (
        <div style={{ display: "flex", gap: 12 }}>
          <button onClick={() => navigate("/dashboard")}>Dashboard</button>

          <button onClick={() => navigate("/logout")}>Logout</button>
        </div>
      ) : (
        <div style={{ display: "flex", gap: 12 }}>
          <button onClick={() => navigate("/login")}>Login</button>

          <button onClick={() => navigate("/register")}>Register</button>
        </div>
      )}
    </div>
  );
}

function Logout() {
  const navigate = useNavigate();

  useEffect(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");

    navigate("/");
  }, [navigate]);

  return <div style={{ padding: 20 }}>Logging out...</div>;
}

function ProtectedRoute({ children }: { children: JSX.Element }) {
  const token = localStorage.getItem("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* ALWAYS PUBLIC */}
        <Route path="/" element={<Home />} />

        {/* PUBLIC AUTH */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* PROTECTED DASHBOARD */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* LOGOUT */}
        <Route path="/logout" element={<Logout />} />
      </Routes>
    </BrowserRouter>
  );
}
