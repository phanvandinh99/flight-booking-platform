import React from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { airlineMenuItems } from "../../config/airlineMenu";

export default function AirlineDashboard() {
  return (
    <DashboardLayout
      menuItems={airlineMenuItems}
      title="Airline Representative Dashboard"
    >
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
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" />
            </svg>
          </div>
          <div className="welcome-content">
            <h2>Chào mừng đến với Airline Dashboard</h2>
            <p>Quản lý chuyến bay và giá vé của hãng hàng không</p>
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
                <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" />
              </svg>
            </div>
            <h3>Quản lý máy bay</h3>
            <p>Thêm, sửa, xóa thông tin máy bay</p>
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
                <path d="M21 16V14C21 12.9 20.1 12 19 12H5C3.9 12 3 12.9 3 14V16C3 17.1 3.9 18 5 18H19C20.1 18 21 17.1 21 16Z" />
                <path d="M7 10L12 15L17 10" />
              </svg>
            </div>
            <h3>Quản lý chuyến bay</h3>
            <p>Tạo và quản lý các chuyến bay</p>
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
                <line x1="12" y1="1" x2="12" y2="23" />
                <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
              </svg>
            </div>
            <h3>Quản lý giá vé</h3>
            <p>Thiết lập và điều chỉnh giá vé</p>
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
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
            </div>
            <h3>Đặt vé và thống kê</h3>
            <p>Xem thống kê đặt vé và báo cáo</p>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
