import React from "react";
import "../../styles/auth.css";

/**
 * Layout chung cho các trang authentication (Login, Register, etc.)
 * @param {Object} props
 * @param {React.ReactNode} props.children - Nội dung trang
 * @param {string} props.title - Tiêu đề trang
 * @param {string} props.subtitle - Phụ đề trang
 */
export default function AuthLayout({
  children,
  title = "Đăng nhập",
  subtitle = "Hệ Thống Quản Lý Chuyến Bay",
}) {
  return (
    <div className="auth-layout">
      <div className="auth-container">
        <div className="auth-header">
          <div className="auth-logo">
            <svg
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M21 16V14C21 12.9 20.1 12 19 12H5C3.9 12 3 12.9 3 14V16C3 17.1 3.9 18 5 18H19C20.1 18 21 17.1 21 16Z"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
              <path
                d="M7 10L12 15L17 10"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </div>
          <h1 className="auth-title">{title}</h1>
          <p className="auth-subtitle">{subtitle}</p>
        </div>
        <div className="auth-content">{children}</div>
      </div>
      <div className="auth-background">
        <div className="auth-background-shape auth-shape-1"></div>
        <div className="auth-background-shape auth-shape-2"></div>
        <div className="auth-background-shape auth-shape-3"></div>
      </div>
    </div>
  );
}
