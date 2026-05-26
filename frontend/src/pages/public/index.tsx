import React, { useEffect, type JSX } from "react";
import { Navigate } from "react-router-dom";
import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";

import Login from "../../pages/public/Login";
import Register from "../../pages/public/Register";
import Dashboard from "../../pages/public/Dashboard";

function Home() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const rawUser = localStorage.getItem("user");
  const user = rawUser ? JSON.parse(rawUser) : null;

  return (
    <div style={{ padding: 20 }}>
      <h1>{token ? `Welcome back, ${user?.name ?? "user"}!` : "Welcome!"}</h1>
      {token ? (
        <button onClick={() => navigate("/logout")}>Logout</button>
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
        {/* PUBLIC INDEX */}
        <Route path="/" element={<Home />} />

        {/* PUBLIC AUTH */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* PROTECTED AREA */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        <Route path="/logout" element={<Logout />} />
      </Routes>
    </BrowserRouter>
  );
}