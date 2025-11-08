import React from "react";
import { useAuth } from "../../auth/AuthContext";
import { logoutApi } from "../../api/auth";

export default function AdminDashboard() {
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
      <h2>Admin Dashboard</h2>
      <p>Xin chào, {user?.ten_day_du || user?.email}</p>
      <div style={{ marginTop: 12 }}>
        <button onClick={handleLogout}>Đăng xuất</button>
      </div>
      <div style={{ marginTop: 24 }}>
        <ul>
          <li>Quản lý phê duyệt hãng</li>
          <li>Quản lý sân bay</li>
          <li>Quản lý tuyến bay</li>
          <li>Báo cáo tổng hợp</li>
          <li>Cấu hình hệ thống</li>
        </ul>
      </div>
    </div>
  );
}
