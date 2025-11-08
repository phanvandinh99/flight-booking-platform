import React, { useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import { logoutApi } from "../../api/auth";
import "../../styles/dashboard.css";

/**
 * Layout chung cho Admin và Airline Representative Dashboard
 * @param {Object} props
 * @param {React.ReactNode} props.children - Nội dung trang
 * @param {Array} props.menuItems - Danh sách menu items cho sidebar
 * @param {string} props.title - Tiêu đề trang
 */
export default function DashboardLayout({
  children,
  menuItems = [],
  title = "Dashboard",
}) {
  const { user, logout } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [activeMenuItem, setActiveMenuItem] = useState(null);

  const handleLogout = async () => {
    try {
      await logoutApi();
    } catch {}
    logout();
    window.location.href = "/login";
  };

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div className="dashboard-layout">
      {/* Sidebar */}
      <aside className={`dashboard-sidebar ${sidebarOpen ? "open" : "closed"}`}>
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <svg
              width="32"
              height="32"
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
            {sidebarOpen && <span className="logo-text">Flight Booking</span>}
          </div>
          <button className="sidebar-toggle" onClick={toggleSidebar}>
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              {sidebarOpen ? (
                <path d="M18 6L6 18M6 6L18 18" />
              ) : (
                <path d="M3 12H21M3 6H21M3 18H21" />
              )}
            </svg>
          </button>
        </div>

        <nav className="sidebar-nav">
          {menuItems.map((item, index) => (
            <div key={index} className="nav-section">
              {item.section && sidebarOpen && (
                <div className="nav-section-title">{item.section}</div>
              )}
              {item.items?.map((menuItem, itemIndex) => (
                <button
                  key={itemIndex}
                  className={`nav-item ${
                    activeMenuItem === `${index}-${itemIndex}` ? "active" : ""
                  }`}
                  onClick={() => {
                    setActiveMenuItem(`${index}-${itemIndex}`);
                    if (menuItem.onClick) {
                      menuItem.onClick();
                    }
                  }}
                  title={!sidebarOpen ? menuItem.label : ""}
                >
                  <span className="nav-icon">{menuItem.icon}</span>
                  {sidebarOpen && (
                    <span className="nav-label">{menuItem.label}</span>
                  )}
                  {menuItem.badge && sidebarOpen && (
                    <span className="nav-badge">{menuItem.badge}</span>
                  )}
                </button>
              ))}
            </div>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">
              {user?.ten_day_du?.charAt(0)?.toUpperCase() ||
                user?.email?.charAt(0)?.toUpperCase() ||
                "U"}
            </div>
            {sidebarOpen && (
              <div className="user-details">
                <div className="user-name">
                  {user?.ten_day_du || user?.email || "User"}
                </div>
                <div className="user-role">
                  {user?.vai_tro === "admin"
                    ? "Quản trị viên"
                    : "Đại diện hãng"}
                </div>
              </div>
            )}
          </div>
          <button
            className="logout-btn"
            onClick={handleLogout}
            title={!sidebarOpen ? "Đăng xuất" : ""}
          >
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
            {sidebarOpen && <span>Đăng xuất</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="dashboard-main">
        {/* Header */}
        <header className="dashboard-header">
          <div className="header-left">
            <h1 className="page-title">{title}</h1>
          </div>
          <div className="header-right">
            <div className="header-actions">
              <button className="header-icon-btn" title="Thông báo">
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                </svg>
                <span className="notification-badge">3</span>
              </button>
            </div>
          </div>
        </header>

        {/* Content */}
        <main className="dashboard-content">{children}</main>
      </div>
    </div>
  );
}
