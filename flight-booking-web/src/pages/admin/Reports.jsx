import React, { useState, useEffect } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { adminMenuItems } from "../../config/adminMenu";
import {
  getRevenueSummary,
  getMonthlyRevenue,
  getTopAirlines,
} from "../../api/admin";
import "../../styles/reports.css";

export default function Reports() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [revenueSummary, setRevenueSummary] = useState(null);
  const [monthlyRevenue, setMonthlyRevenue] = useState([]);
  const [topAirlines, setTopAirlines] = useState([]);
  const [dateRange, setDateRange] = useState({
    tu_ngay: new Date(new Date().getFullYear(), 0, 1)
      .toISOString()
      .split("T")[0],
    den_ngay: new Date().toISOString().split("T")[0],
  });

  useEffect(() => {
    loadReports();
  }, [dateRange]);

  const loadReports = async () => {
    try {
      setLoading(true);
      setError(null);
      const [summaryRes, monthlyRes, topAirlinesRes] = await Promise.all([
        getRevenueSummary(),
        getMonthlyRevenue(dateRange),
        getTopAirlines(10),
      ]);
      setRevenueSummary(summaryRes.data);
      setMonthlyRevenue(monthlyRes.data || []);
      setTopAirlines(topAirlinesRes.data || []);
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

  return (
    <DashboardLayout menuItems={adminMenuItems} title="Báo Cáo Tổng Hợp">
      <div className="reports-page">
        <div className="page-header">
          <div className="header-content">
            <h2>Báo Cáo Tổng Hợp</h2>
            <p>Xem thống kê doanh thu và hoạt động của hệ thống</p>
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

        {loading && !revenueSummary ? (
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
            <h3>Không thể tải dữ liệu</h3>
            <p>{error}</p>
            <button className="btn-retry" onClick={loadReports}>
              Thử lại
            </button>
          </div>
        ) : (
          <>
            {/* Summary Cards */}
            <div className="summary-cards">
              <div className="summary-card revenue-card">
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
                  <h3>Tổng Doanh Thu</h3>
                  <p className="card-value">
                    {formatCurrency(revenueSummary?.tong_doanh_thu)}
                  </p>
                  <span className="card-label">
                    Từ {revenueSummary?.tong_dat_ve_da_thanh_toan || 0} đặt vé
                    đã thanh toán
                  </span>
                </div>
              </div>

              <div className="summary-card bookings-card">
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
                    <polyline points="14 2 14 8 20 8" />
                    <line x1="16" y1="13" x2="8" y2="13" />
                    <line x1="16" y1="17" x2="8" y2="17" />
                    <polyline points="10 9 9 9 8 9" />
                  </svg>
                </div>
                <div className="card-content">
                  <h3>Tổng Đặt Vé</h3>
                  <p className="card-value">
                    {formatNumber(revenueSummary?.tong_dat_ve)}
                  </p>
                  <span className="card-label">
                    {revenueSummary?.tong_dat_ve_da_thanh_toan || 0} đã thanh
                    toán
                  </span>
                </div>
              </div>

              <div className="summary-card average-card">
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
                  <h3>Doanh Thu Trung Bình</h3>
                  <p className="card-value">
                    {formatCurrency(revenueSummary?.doanh_thu_trung_binh)}
                  </p>
                  <span className="card-label">Trên mỗi đặt vé</span>
                </div>
              </div>

              <div className="summary-card rate-card">
                <div className="card-icon">
                  <svg
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
                  </svg>
                </div>
                <div className="card-content">
                  <h3>Tỷ Lệ Thanh Toán</h3>
                  <p className="card-value">
                    {revenueSummary?.tong_dat_ve > 0
                      ? (
                          (revenueSummary.tong_dat_ve_da_thanh_toan /
                            revenueSummary.tong_dat_ve) *
                          100
                        ).toFixed(1)
                      : 0}
                    %
                  </p>
                  <span className="card-label">
                    {revenueSummary?.tong_dat_ve_da_thanh_toan || 0} /{" "}
                    {revenueSummary?.tong_dat_ve || 0} đặt vé
                  </span>
                </div>
              </div>
            </div>

            {/* Monthly Revenue Table */}
            <div className="report-section">
              <div className="section-header">
                <h3>Doanh Thu Theo Tháng</h3>
                <span className="section-subtitle">
                  Từ {new Date(dateRange.tu_ngay).toLocaleDateString("vi-VN")}{" "}
                  đến {new Date(dateRange.den_ngay).toLocaleDateString("vi-VN")}
                </span>
              </div>
              <div className="table-container">
                <table className="reports-table">
                  <thead>
                    <tr>
                      <th>Tháng</th>
                      <th>Số Đơn</th>
                      <th>Doanh Thu</th>
                      <th>Trung Bình/Đơn</th>
                    </tr>
                  </thead>
                  <tbody>
                    {monthlyRevenue.length === 0 ? (
                      <tr>
                        <td colSpan="4" className="empty-row">
                          Không có dữ liệu trong khoảng thời gian này
                        </td>
                      </tr>
                    ) : (
                      monthlyRevenue.map((item, index) => (
                        <tr key={index}>
                          <td>
                            <span className="month-label">
                              {new Date(item.month + "-01").toLocaleDateString(
                                "vi-VN",
                                { month: "long", year: "numeric" }
                              )}
                            </span>
                          </td>
                          <td>{formatNumber(item.orders)}</td>
                          <td className="revenue-cell">
                            {formatCurrency(item.revenue)}
                          </td>
                          <td>
                            {formatCurrency(
                              item.orders > 0 ? item.revenue / item.orders : 0
                            )}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Top Airlines Table */}
            <div className="report-section">
              <div className="section-header">
                <h3>Top Hãng Hàng Không</h3>
                <span className="section-subtitle">
                  Top 10 hãng có doanh thu cao nhất
                </span>
              </div>
              <div className="table-container">
                <table className="reports-table">
                  <thead>
                    <tr>
                      <th>Hạng</th>
                      <th>Mã Hãng</th>
                      <th>Tên Hãng</th>
                      <th>Số Đặt Vé</th>
                      <th>Tổng Doanh Thu</th>
                      <th>Trung Bình/Đơn</th>
                    </tr>
                  </thead>
                  <tbody>
                    {topAirlines.length === 0 ? (
                      <tr>
                        <td colSpan="6" className="empty-row">
                          Không có dữ liệu
                        </td>
                      </tr>
                    ) : (
                      topAirlines.map((airline, index) => (
                        <tr key={airline.id}>
                          <td>
                            <span className={`rank-badge rank-${index + 1}`}>
                              {index + 1}
                            </span>
                          </td>
                          <td>
                            <span className="airline-code">
                              {airline.ma_hang}
                            </span>
                          </td>
                          <td>{airline.ten_hang}</td>
                          <td>{formatNumber(airline.so_dat_ve)}</td>
                          <td className="revenue-cell">
                            {formatCurrency(airline.tong_doanh_thu)}
                          </td>
                          <td>
                            {formatCurrency(airline.doanh_thu_trung_binh)}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </div>
    </DashboardLayout>
  );
}
