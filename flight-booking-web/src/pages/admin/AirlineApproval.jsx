import React, { useState, useEffect } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { adminMenuItems } from "../../config/adminMenu";
import {
  getPendingAirlines,
  approveAirline,
  rejectAirline,
  activateAirline,
  suspendAirline,
} from "../../api/admin";
import "../../styles/airlineApproval.css";

export default function AirlineApproval() {
  const [airlines, setAirlines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState({});

  useEffect(() => {
    loadAirlines();
  }, []);

  const loadAirlines = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getPendingAirlines();
      setAirlines(response.data || []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Không thể tải danh sách hãng hàng không"
      );
      console.error("Error loading airlines:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (id, action, actionName) => {
    try {
      setActionLoading({ ...actionLoading, [id]: true });
      let response;

      switch (action) {
        case "approve":
          response = await approveAirline(id);
          break;
        case "reject":
          response = await rejectAirline(id);
          break;
        case "activate":
          response = await activateAirline(id);
          break;
        case "suspend":
          response = await suspendAirline(id);
          break;
        default:
          return;
      }

      // Reload danh sách sau khi thực hiện action
      await loadAirlines();
    } catch (err) {
      alert(
        err.response?.data?.message || `Không thể ${actionName} hãng hàng không`
      );
      console.error(`Error ${actionName}:`, err);
    } finally {
      setActionLoading({ ...actionLoading, [id]: false });
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      cho_duyet: { label: "Chờ duyệt", class: "status-pending" },
      hoat_dong: { label: "Hoạt động", class: "status-active" },
      dinh_chi: { label: "Đình chỉ", class: "status-suspended" },
      tu_choi: { label: "Từ chối", class: "status-rejected" },
    };

    const config = statusConfig[status] || {
      label: status,
      class: "status-default",
    };
    return (
      <span className={`status-badge ${config.class}`}>{config.label}</span>
    );
  };

  const getActionButtons = (airline) => {
    const { id, trang_thai } = airline;
    const isLoading = actionLoading[id];

    switch (trang_thai) {
      case "cho_duyet":
      case null:
        return (
          <div className="action-buttons">
            <button
              className="btn-approve"
              onClick={() => handleAction(id, "approve", "phê duyệt")}
              disabled={isLoading}
            >
              {isLoading ? (
                <span className="loading-spinner"></span>
              ) : (
                <>
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                    <polyline points="22 4 12 14.01 9 11.01" />
                  </svg>
                  Phê duyệt
                </>
              )}
            </button>
            <button
              className="btn-reject"
              onClick={() => handleAction(id, "reject", "từ chối")}
              disabled={isLoading}
            >
              {isLoading ? (
                <span className="loading-spinner"></span>
              ) : (
                <>
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <line x1="15" y1="9" x2="9" y2="15" />
                    <line x1="9" y1="9" x2="15" y2="15" />
                  </svg>
                  Từ chối
                </>
              )}
            </button>
          </div>
        );

      case "hoat_dong":
        return (
          <div className="action-buttons">
            <button
              className="btn-suspend"
              onClick={() => handleAction(id, "suspend", "đình chỉ")}
              disabled={isLoading}
            >
              {isLoading ? (
                <span className="loading-spinner"></span>
              ) : (
                <>
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="8" x2="12" y2="16" />
                    <line x1="8" y1="12" x2="16" y2="12" />
                  </svg>
                  Đình chỉ
                </>
              )}
            </button>
          </div>
        );

      case "dinh_chi":
        return (
          <div className="action-buttons">
            <button
              className="btn-activate"
              onClick={() => handleAction(id, "activate", "kích hoạt")}
              disabled={isLoading}
            >
              {isLoading ? (
                <span className="loading-spinner"></span>
              ) : (
                <>
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                  Kích hoạt
                </>
              )}
            </button>
          </div>
        );

      case "tu_choi":
        return (
          <div className="action-buttons">
            <button
              className="btn-activate"
              onClick={() => handleAction(id, "approve", "phê duyệt lại")}
              disabled={isLoading}
            >
              {isLoading ? (
                <span className="loading-spinner"></span>
              ) : (
                <>
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                    <polyline points="22 4 12 14.01 9 11.01" />
                  </svg>
                  Phê duyệt lại
                </>
              )}
            </button>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <DashboardLayout
      menuItems={adminMenuItems}
      title="Phê duyệt Hãng Hàng Không"
    >
      <div className="airline-approval-page">
        <div className="page-header">
          <div className="header-content">
            <h2>Quản lý Phê duyệt Hãng Hàng Không</h2>
            <p>Xem và phê duyệt các hãng hàng không đăng ký mới</p>
          </div>
          <button
            className="btn-refresh"
            onClick={loadAirlines}
            disabled={loading}
          >
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <polyline points="23 4 23 10 17 10" />
              <polyline points="1 20 1 14 7 14" />
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
            </svg>
            Làm mới
          </button>
        </div>

        {loading && airlines.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách hãng hàng không...</p>
          </div>
        ) : error ? (
          <div className="error-container">
            <div className="error-icon">
              <svg
                width="48"
                height="48"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>
            <h3>Không thể tải dữ liệu</h3>
            <p>{error}</p>
            <button className="btn-retry" onClick={loadAirlines}>
              Thử lại
            </button>
          </div>
        ) : airlines.length === 0 ? (
          <div className="empty-container">
            <div className="empty-icon">
              <svg
                width="64"
                height="64"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" />
              </svg>
            </div>
            <h3>Không có hãng hàng không nào</h3>
            <p>Hiện tại không có hãng hàng không nào cần phê duyệt</p>
          </div>
        ) : (
          <div className="airlines-grid">
            {airlines.map((airline) => (
              <div key={airline.id} className="airline-card">
                <div className="card-header">
                  <div className="airline-info">
                    <div className="airline-logo">
                      {airline.ten_hang?.charAt(0) || "A"}
                    </div>
                    <div className="airline-details">
                      <h3>{airline.ten_hang || "Chưa có tên"}</h3>
                      <p className="airline-code">{airline.ma_hang || "N/A"}</p>
                    </div>
                  </div>
                  {getStatusBadge(airline.trang_thai)}
                </div>

                <div className="card-body">
                  {airline.mo_ta && (
                    <p className="airline-description">{airline.mo_ta}</p>
                  )}
                  <div className="airline-meta">
                    {airline.quoc_gia && (
                      <div className="meta-item">
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                        >
                          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                          <circle cx="12" cy="10" r="3" />
                        </svg>
                        <span>{airline.quoc_gia}</span>
                      </div>
                    )}
                    {airline.website && (
                      <div className="meta-item">
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                        >
                          <circle cx="12" cy="12" r="10" />
                          <line x1="2" y1="12" x2="22" y2="12" />
                          <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
                        </svg>
                        <a
                          href={airline.website}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          Website
                        </a>
                      </div>
                    )}
                  </div>
                </div>

                <div className="card-footer">{getActionButtons(airline)}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
