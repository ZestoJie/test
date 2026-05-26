import { Navigate } from "react-router-dom";

export default function ProtectedRoute({ children }: any) {
  const token = localStorage.getItem("token");

  // ❌ NOT LOGGED IN → send to login page
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // ✅ LOGGED IN → allow page
  return children;
}
