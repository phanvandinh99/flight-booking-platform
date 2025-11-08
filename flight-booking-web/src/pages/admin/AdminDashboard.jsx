import React from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { adminMenuItems } from "../../config/adminMenu";

export default function AdminDashboard() {
  return (
    <DashboardLayout menuItems={adminMenuItems} title="Trang Quản Trị">
      <div className="dashboard-welcome">
        <div className="welcome-card">
          <div className="welcome-icon">
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
          <div className="welcome-content">
            <h2>Chào mừng đến với Trang Quản Trị</h2>
            <p>Quản lý hệ thống đặt vé máy bay một cách hiệu quả</p>
          </div>
        </div>

        <div className="dashboard-grid">
          <div className="dashboard-card">
            <div
              className="card-icon"
              style={{
                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
              }}
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
            </div>
            <h3>Phê duyệt hãng</h3>
            <p>Quản lý và phê duyệt các hãng hàng không mới</p>
          </div>

          <div className="dashboard-card">
            <div
              className="card-icon"
              style={{
                background: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
              }}
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                <circle cx="12" cy="10" r="3" />
              </svg>
            </div>
            <h3>Quản lý sân bay</h3>
            <p>Thêm, sửa, xóa thông tin sân bay</p>
          </div>

          <div className="dashboard-card">
            <div
              className="card-icon"
              style={{
                background: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)",
              }}
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M21 16V14C21 12.9 20.1 12 19 12H5C3.9 12 3 12.9 3 14V16C3 17.1 3.9 18 5 18H19C20.1 18 21 17.1 21 16Z" />
                <path d="M7 10L12 15L17 10" />
              </svg>
            </div>
            <h3>Quản lý tuyến bay</h3>
            <p>Thiết lập và quản lý các tuyến bay</p>
          </div>

          <div className="dashboard-card">
            <div
              className="card-icon"
              style={{
                background: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)",
              }}
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <line x1="18" y1="20" x2="18" y2="10" />
                <line x1="12" y1="20" x2="12" y2="4" />
                <line x1="6" y1="20" x2="6" y2="14" />
              </svg>
            </div>
            <h3>Báo cáo tổng hợp</h3>
            <p>Xem các báo cáo và thống kê hệ thống</p>
          </div>

          <div className="dashboard-card">
            <div
              className="card-icon"
              style={{
                background: "linear-gradient(135deg, #fa709a 0%, #fee140 100%)",
              }}
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="12" cy="12" r="3" />
                <path d="M12 1v6m0 6v6M5.64 5.64l4.24 4.24m4.24 4.24l4.24 4.24M1 12h6m6 0h6M5.64 18.36l4.24-4.24m4.24-4.24l4.24-4.24" />
              </svg>
            </div>
            <h3>Cấu hình hệ thống</h3>
            <p>Thiết lập các thông số hệ thống</p>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
