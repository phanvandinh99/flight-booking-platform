import React from "react";
import { useAuth } from "../../auth/AuthContext";
import { logoutApi } from "../../api/auth";

export default function AirlineDashboard() {
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    try {
      await logoutApi();
    } catch {}
    logout();
    window.location.href = "/login";
  };

  return (
    <div style={{ padding: 24 }}>
      <h2>Airline Representative Dashboard</h2>
      <p>Xin chào, {user?.ten_day_du || user?.email}</p>
      <div style={{ marginTop: 12 }}>
        <button onClick={handleLogout}>Đăng xuất</button>
      </div>
      <div style={{ marginTop: 24 }}>
        <ul>
          <li>Quản lý máy bay</li>
          <li>Quản lý chuyến bay</li>
          <li>Quản lý giá vé</li>
          <li>Đặt vé và thống kê</li>
        </ul>
      </div>
    </div>
  );
}
