import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function ProtectedRoute({ allowRoles }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowRoles && !allowRoles.includes(user.vai_tro)) {
    // Redirect user to their home based on role if blocked
    if (user.vai_tro === "admin") return <Navigate to="/admin" replace />;
    if (user.vai_tro === "dai_dien_hang")
      return <Navigate to="/airline" replace />;
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
