import React, { useState, useEffect } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { airlineMenuItems } from "../../config/airlineMenu";
import {
  getDailyRevenue,
  getWeeklyRevenue,
  getMonthlyRevenue,
  getFlightReport,
  getFareClassReport,
  getOverviewReport,
} from "../../api/airline";
import "../../styles/airlineReports.css";

export default function AirlineReports() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState("overview");
  const [dateRange, setDateRange] = useState({
    tu_ngay: new Date(new Date().getFullYear(), new Date().getMonth(), 1)
      .toISOString()
      .split("T")[0],
    den_ngay: new Date().toISOString().split("T")[0],
  });

  // Data states
  const [overview, setOverview] = useState(null);
  const [dailyRevenue, setDailyRevenue] = useState([]);
  const [weeklyRevenue, setWeeklyRevenue] = useState([]);
  const [monthlyRevenue, setMonthlyRevenue] = useState([]);
  const [flightReport, setFlightReport] = useState([]);
  const [fareClassReport, setFareClassReport] = useState([]);

  useEffect(() => {
    loadReports();
  }, [dateRange, activeTab]);

  const loadReports = async () => {
    try {
      setLoading(true);
      setError(null);

      const params = {
        tu_ngay: dateRange.tu_ngay,
        den_ngay: dateRange.den_ngay,
      };

      switch (activeTab) {
        case "overview":
          const overviewRes = await getOverviewReport(params);
          setOverview(overviewRes.data || overviewRes);
          break;
        case "daily":
          const dailyRes = await getDailyRevenue(params);
          setDailyRevenue(dailyRes.data || dailyRes || []);
          break;
        case "weekly":
          const weeklyRes = await getWeeklyRevenue(params);
          setWeeklyRevenue(weeklyRes.data || weeklyRes || []);
          break;
        case "monthly":
          const monthlyRes = await getMonthlyRevenue(params);
          setMonthlyRevenue(monthlyRes.data || monthlyRes || []);
          break;
        case "flights":
          const flightRes = await getFlightReport(params);
          setFlightReport(flightRes.data || flightRes || []);
          break;
        case "fare-class":
          const fareClassRes = await getFareClassReport(params);
          setFareClassReport(fareClassRes.data || fareClassRes || []);
          break;
      }
    } catch (err) {
      setError(err.response?.data?.message || "Không thể tải báo cáo");
      console.error("Error loading reports:", err);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount || 0);
  };

  const formatNumber = (num) => {
    return new Intl.NumberFormat("vi-VN").format(num || 0);
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const getHangVeLabel = (hangVe) => {
    const labels = {
      pho_thong: "Phổ thông",
      thuong_gia: "Thương gia",
      hang_nhat: "Hạng nhất",
    };
    return labels[hangVe] || hangVe;
  };

  return (
    <DashboardLayout menuItems={airlineMenuItems} title="Báo Cáo & Thống Kê">
      <div className="airline-reports-page">
        <div className="page-header">
          <div className="header-content">
            <h2>Báo Cáo & Thống Kê</h2>
            <p>Xem báo cáo doanh thu và thống kê hoạt động</p>
          </div>
          <div className="header-actions">
            <div className="date-range-filter">
              <label>Từ ngày:</label>
              <input
                type="date"
                value={dateRange.tu_ngay}
                onChange={(e) =>
                  setDateRange({ ...dateRange, tu_ngay: e.target.value })
                }
              />
              <label>Đến ngày:</label>
              <input
                type="date"
                value={dateRange.den_ngay}
                onChange={(e) =>
                  setDateRange({ ...dateRange, den_ngay: e.target.value })
                }
              />
            </div>
            <button className="btn-refresh" onClick={loadReports}>
              <svg
                width="16"
                height="16"
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
        </div>

        {/* Tabs */}
        <div className="tabs-container">
          <button
            className={`tab-btn ${activeTab === "overview" ? "active" : ""}`}
            onClick={() => setActiveTab("overview")}
          >
            Tổng quan
          </button>
          <button
            className={`tab-btn ${activeTab === "daily" ? "active" : ""}`}
            onClick={() => setActiveTab("daily")}
          >
            Theo ngày
          </button>
          <button
            className={`tab-btn ${activeTab === "weekly" ? "active" : ""}`}
            onClick={() => setActiveTab("weekly")}
          >
            Theo tuần
          </button>
          <button
            className={`tab-btn ${activeTab === "monthly" ? "active" : ""}`}
            onClick={() => setActiveTab("monthly")}
          >
            Theo tháng
          </button>
          <button
            className={`tab-btn ${activeTab === "flights" ? "active" : ""}`}
            onClick={() => setActiveTab("flights")}
          >
            Theo chuyến bay
          </button>
          <button
            className={`tab-btn ${activeTab === "fare-class" ? "active" : ""}`}
            onClick={() => setActiveTab("fare-class")}
          >
            Theo hạng vé
          </button>
        </div>

        {loading ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải báo cáo...</p>
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
            <h3>Không thể tải báo cáo</h3>
            <p>{error}</p>
            <button className="btn-retry" onClick={loadReports}>
              Thử lại
            </button>
          </div>
        ) : (
          <div className="reports-content">
            {/* Overview Tab */}
            {activeTab === "overview" && overview && (
              <div className="overview-section">
                <div className="overview-cards">
                  <div className="overview-card">
                    <div className="card-icon">
                      <svg
                        width="24"
                        height="24"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                      >
                        <path d="M21 16V14C21 12.9 20.1 12 19 12H5C3.9 12 3 12.9 3 14V16C3 17.1 3.9 18 5 18H19C20.1 18 21 17.1 21 16Z" />
                      </svg>
                    </div>
                    <div className="card-content">
                      <h3>Tổng số chuyến bay</h3>
                      <p className="card-value">
                        {formatNumber(overview.tong_so_chuyen_bay || 0)}
                      </p>
                    </div>
                  </div>

                  <div className="overview-card">
                    <div className="card-icon">
                      <svg
                        width="24"
                        height="24"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                      >
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                      </svg>
                    </div>
                    <div className="card-content">
                      <h3>Tổng số đặt vé</h3>
                      <p className="card-value">
                        {formatNumber(overview.tong_so_dat_ve || 0)}
                      </p>
                    </div>
                  </div>

                  <div className="overview-card">
                    <div className="card-icon">
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
                    <div className="card-content">
                      <h3>Tổng doanh thu</h3>
                      <p className="card-value">
                        {formatCurrency(overview.tong_doanh_thu || 0)}
                      </p>
                    </div>
                  </div>

                  <div className="overview-card">
                    <div className="card-icon">
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
                    <div className="card-content">
                      <h3>Doanh thu trung bình</h3>
                      <p className="card-value">
                        {formatCurrency(overview.doanh_thu_trung_binh || 0)}
                      </p>
                    </div>
                  </div>

                  {overview.ty_le_thanh_cong !== undefined && (
                    <div className="overview-card">
                      <div className="card-icon">
                        <svg
                          width="24"
                          height="24"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                        >
                          <circle cx="12" cy="12" r="10" />
                          <polyline points="12 6 12 12 16 14" />
                        </svg>
                      </div>
                      <div className="card-content">
                        <h3>Tỷ lệ thành công</h3>
                        <p className="card-value">
                          {overview.ty_le_thanh_cong?.toFixed(1) || 0}%
                        </p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Daily Revenue Tab */}
            {activeTab === "daily" && (
              <div className="report-table-container">
                <table className="report-table">
                  <thead>
                    <tr>
                      <th>Ngày</th>
                      <th>Số đặt vé</th>
                      <th>Doanh thu</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dailyRevenue.length === 0 ? (
                      <tr>
                        <td colSpan="3" className="empty-row">
                          Không có dữ liệu trong khoảng thời gian này
                        </td>
                      </tr>
                    ) : (
                      dailyRevenue.map((item, index) => (
                        <tr key={index}>
                          <td>{formatDate(item.ngay)}</td>
                          <td>{formatNumber(item.so_dat_ve || 0)}</td>
                          <td className="revenue-cell">
                            {formatCurrency(item.doanh_thu || 0)}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}

            {/* Weekly Revenue Tab */}
            {activeTab === "weekly" && (
              <div className="report-table-container">
                <table className="report-table">
                  <thead>
                    <tr>
                      <th>Tuần</th>
                      <th>Số đặt vé</th>
                      <th>Doanh thu</th>
                    </tr>
                  </thead>
                  <tbody>
                    {weeklyRevenue.length === 0 ? (
                      <tr>
                        <td colSpan="3" className="empty-row">
                          Không có dữ liệu trong khoảng thời gian này
                        </td>
                      </tr>
                    ) : (
                      weeklyRevenue.map((item, index) => {
                        // Format tuần - có thể có nam và tuan riêng
                        let weekLabel = "";
                        if (item.tuan) {
                          if (item.nam) {
                            weekLabel = `Tuần ${item.tuan}, ${item.nam}`;
                          } else {
                            weekLabel = `Tuần ${item.tuan}`;
                          }
                        } else {
                          weekLabel = `Tuần ${index + 1}`;
                        }

                        return (
                          <tr key={index}>
                            <td>{weekLabel}</td>
                            <td>{formatNumber(item.so_dat_ve || 0)}</td>
                            <td className="revenue-cell">
                              {formatCurrency(item.doanh_thu || 0)}
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            )}

            {/* Monthly Revenue Tab */}
            {activeTab === "monthly" && (
              <div className="report-table-container">
                <table className="report-table">
                  <thead>
                    <tr>
                      <th>Tháng</th>
                      <th>Số đặt vé</th>
                      <th>Doanh thu</th>
                    </tr>
                  </thead>
                  <tbody>
                    {monthlyRevenue.length === 0 ? (
                      <tr>
                        <td colSpan="3" className="empty-row">
                          Không có dữ liệu trong khoảng thời gian này
                        </td>
                      </tr>
                    ) : (
                      monthlyRevenue.map((item, index) => {
                        // Xử lý format tháng - có thể là "2025-11" hoặc có nam và thang riêng
                        let monthLabel = "";
                        if (item.thang) {
                          // Nếu có thang dạng "2025-11"
                          if (
                            typeof item.thang === "string" &&
                            item.thang.includes("-")
                          ) {
                            monthLabel = new Date(
                              item.thang + "-01"
                            ).toLocaleDateString("vi-VN", {
                              month: "long",
                              year: "numeric",
                            });
                          } else {
                            // Nếu có nam và thang riêng
                            const year = item.nam || new Date().getFullYear();
                            const month = item.thang;
                            monthLabel = new Date(
                              year,
                              month - 1,
                              1
                            ).toLocaleDateString("vi-VN", {
                              month: "long",
                              year: "numeric",
                            });
                          }
                        } else {
                          monthLabel = `Tháng ${index + 1}`;
                        }

                        return (
                          <tr key={index}>
                            <td>{monthLabel}</td>
                            <td>{formatNumber(item.so_dat_ve || 0)}</td>
                            <td className="revenue-cell">
                              {formatCurrency(item.doanh_thu || 0)}
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            )}

            {/* Flight Report Tab */}
            {activeTab === "flights" && (
              <div className="report-table-container">
                <table className="report-table">
                  <thead>
                    <tr>
                      <th>Mã chuyến bay</th>
                      <th>Tuyến bay</th>
                      <th>Số đặt vé</th>
                      <th>Tổng doanh thu</th>
                    </tr>
                  </thead>
                  <tbody>
                    {flightReport.length === 0 ? (
                      <tr>
                        <td colSpan="4" className="empty-row">
                          Không có dữ liệu trong khoảng thời gian này
                        </td>
                      </tr>
                    ) : (
                      flightReport.map((item, index) => (
                        <tr key={item.id || index}>
                          <td>
                            <span className="flight-code">
                              {item.ma_chuyen_bay || "N/A"}
                            </span>
                          </td>
                          <td>
                            {item.tuyen_bay?.san_bay_di?.ma_san_bay || "N/A"} →{" "}
                            {item.tuyen_bay?.san_bay_den?.ma_san_bay || "N/A"}
                          </td>
                          <td>{formatNumber(item.so_dat_ve || 0)}</td>
                          <td className="revenue-cell">
                            {formatCurrency(item.tong_doanh_thu || 0)}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}

            {/* Fare Class Report Tab */}
            {activeTab === "fare-class" && (
              <div className="report-table-container">
                <table className="report-table">
                  <thead>
                    <tr>
                      <th>Hạng vé</th>
                      <th>Số đặt vé</th>
                      <th>Doanh thu</th>
                      <th>Giá trung bình</th>
                    </tr>
                  </thead>
                  <tbody>
                    {fareClassReport.length === 0 ? (
                      <tr>
                        <td colSpan="4" className="empty-row">
                          Không có dữ liệu trong khoảng thời gian này
                        </td>
                      </tr>
                    ) : (
                      fareClassReport.map((item, index) => (
                        <tr key={index}>
                          <td>
                            <span className="fare-badge">
                              {getHangVeLabel(item.hang_ve)}
                            </span>
                          </td>
                          <td>{formatNumber(item.so_dat_ve || 0)}</td>
                          <td className="revenue-cell">
                            {formatCurrency(item.doanh_thu || 0)}
                          </td>
                          <td>{formatCurrency(item.gia_trung_binh || 0)}</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
