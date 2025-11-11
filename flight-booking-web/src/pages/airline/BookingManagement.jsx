import React, { useState, useEffect } from "react";
import { createPortal } from "react-dom";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { airlineMenuItems } from "../../config/airlineMenu";
import {
  getBookings,
  getBooking,
  updateBookingStatus,
  getBookingStatistics,
  getFlightsForBookings,
} from "../../api/airline";
import "../../styles/bookingManagement.css";

export default function BookingManagement() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDirection, setSortDirection] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);

  // Filter states
  const [filterChuyenBay, setFilterChuyenBay] = useState("");
  const [filterTrangThai, setFilterTrangThai] = useState("");
  const [filterNgayDat, setFilterNgayDat] = useState("");

  // Statistics
  const [statistics, setStatistics] = useState(null);
  const [loadingStats, setLoadingStats] = useState(false);
  const [dateRange, setDateRange] = useState({
    tu_ngay: new Date(new Date().getFullYear(), new Date().getMonth(), 1)
      .toISOString()
      .split("T")[0],
    den_ngay: new Date().toISOString().split("T")[0],
  });

  // Dropdown data
  const [flights, setFlights] = useState([]);

  useEffect(() => {
    loadBookings();
    loadFlights();
    loadStatistics();
  }, []);

  useEffect(() => {
    loadBookings();
  }, [filterChuyenBay, filterTrangThai, filterNgayDat]);

  useEffect(() => {
    loadStatistics();
  }, [dateRange]);

  const loadBookings = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = {};
      if (filterChuyenBay) params.ma_chuyen_bay = filterChuyenBay;
      if (filterTrangThai) params.trang_thai = filterTrangThai;
      if (filterNgayDat) params.ngay_dat = filterNgayDat;
      if (searchTerm) params.ma_dat_ve = searchTerm;

      const response = await getBookings(params);
      setBookings(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Không thể tải danh sách đặt vé");
      console.error("Error loading bookings:", err);
    } finally {
      setLoading(false);
    }
  };

  const loadFlights = async () => {
    try {
      const response = await getFlightsForBookings();
      setFlights(response.data || []);
    } catch (err) {
      console.error("Error loading flights:", err);
    }
  };

  const loadStatistics = async () => {
    try {
      setLoadingStats(true);
      const response = await getBookingStatistics({
        tu_ngay: dateRange.tu_ngay,
        den_ngay: dateRange.den_ngay,
      });
      setStatistics(response.data || null);
    } catch (err) {
      console.error("Error loading statistics:", err);
    } finally {
      setLoadingStats(false);
    }
  };

  const handleViewDetail = async (id) => {
    try {
      setLoadingDetail(true);
      const response = await getBooking(id);
      setSelectedBooking(response.data || response);
      setShowDetailModal(true);
    } catch (err) {
      alert(err.response?.data?.message || "Không thể tải chi tiết đặt vé");
      console.error("Error loading booking detail:", err);
    } finally {
      setLoadingDetail(false);
    }
  };

  const handleUpdateStatus = async (id, newStatus) => {
    if (
      !window.confirm(
        `Bạn có chắc chắn muốn cập nhật trạng thái đặt vé thành "${getTrangThaiLabel(
          newStatus
        )}"?`
      )
    ) {
      return;
    }

    try {
      await updateBookingStatus(id, newStatus);
      await loadBookings();
      if (selectedBooking && selectedBooking.id === id) {
        const updated = await getBooking(id);
        setSelectedBooking(updated.data || updated);
      }
      alert("Cập nhật trạng thái thành công");
    } catch (err) {
      alert(err.response?.data?.message || "Không thể cập nhật trạng thái");
      console.error("Error updating status:", err);
    }
  };

  const filteredBookings = bookings.filter((booking) => {
    const search = searchTerm.toLowerCase();
    return (
      booking.ma_dat_ve?.toLowerCase().includes(search) ||
      booking.chuyen_bay?.ma_chuyen_bay?.toLowerCase().includes(search) ||
      booking.khach_hang?.ten?.toLowerCase().includes(search) ||
      booking.khach_hang?.email?.toLowerCase().includes(search)
    );
  });

  // Sort bookings
  const sortedBookings = [...filteredBookings].sort((a, b) => {
    if (!sortField) return 0;

    let aValue, bValue;
    if (sortField === "ngay_dat") {
      aValue = new Date(a.created_at || a.ngay_dat || 0).getTime();
      bValue = new Date(b.created_at || b.ngay_dat || 0).getTime();
    } else if (sortField === "tong_tien") {
      aValue = parseFloat(a.tong_tien || 0);
      bValue = parseFloat(b.tong_tien || 0);
    } else if (sortField === "trang_thai") {
      aValue = a.trang_thai?.toLowerCase() || "";
      bValue = b.trang_thai?.toLowerCase() || "";
    } else {
      aValue = a[sortField]?.toString().toLowerCase() || "";
      bValue = b[sortField]?.toString().toLowerCase() || "";
    }

    if (sortDirection === "asc") {
      return aValue > bValue ? 1 : aValue < bValue ? -1 : 0;
    } else {
      return aValue < bValue ? 1 : aValue > bValue ? -1 : 0;
    }
  });

  // Pagination
  const totalPages = Math.ceil(sortedBookings.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedBookings = sortedBookings.slice(startIndex, endIndex);

  useEffect(() => {
    setCurrentPage(1);
  }, [
    searchTerm,
    sortField,
    sortDirection,
    filterChuyenBay,
    filterTrangThai,
    filterNgayDat,
  ]);

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDirection("asc");
    }
  };

  const getSortIcon = (field) => {
    if (sortField !== field) {
      return (
        <svg
          width="12"
          height="12"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          opacity="0.3"
        >
          <path d="M7 13l5 5 5-5M7 6l5-5 5 5" />
        </svg>
      );
    }
    return sortDirection === "asc" ? (
      <svg
        width="12"
        height="12"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      >
        <path d="M7 13l5 5 5-5M7 6l5-5 5 5" />
      </svg>
    ) : (
      <svg
        width="12"
        height="12"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      >
        <path d="M7 6l5 5 5-5M7 13l5 5 5-5" />
      </svg>
    );
  };

  const getTrangThaiLabel = (trangThai) => {
    const labels = {
      giu_cho: "Giữ chỗ",
      da_thanh_toan: "Đã thanh toán",
      da_huy: "Đã hủy",
    };
    return labels[trangThai] || trangThai;
  };

  const getTrangThaiBadgeClass = (trangThai) => {
    const classes = {
      giu_cho: "status-badge status-hold",
      da_thanh_toan: "status-badge status-paid",
      da_huy: "status-badge status-cancelled",
    };
    return classes[trangThai] || "status-badge";
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount || 0);
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <DashboardLayout menuItems={airlineMenuItems} title="Quản lý Đặt vé">
      <div className="booking-management-page">
        {/* Statistics Cards */}
        <div className="statistics-cards">
          <div className="stat-card">
            <div className="stat-icon revenue">
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
            <div className="stat-content">
              <h3>Tổng doanh thu</h3>
              <p className="stat-value">
                {loadingStats
                  ? "..."
                  : formatCurrency(statistics?.tong_doanh_thu || 0)}
              </p>
              <span className="stat-label">
                {statistics?.da_thanh_toan || 0} đặt vé đã thanh toán
              </span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon bookings">
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
              </svg>
            </div>
            <div className="stat-content">
              <h3>Tổng đặt vé</h3>
              <p className="stat-value">{statistics?.tong_so_dat_ve || 0}</p>
              <span className="stat-label">Trong khoảng thời gian</span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon hold">
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
            <div className="stat-content">
              <h3>Giữ chỗ</h3>
              <p className="stat-value">{statistics?.giu_cho || 0}</p>
              <span className="stat-label">Chưa thanh toán</span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon cancelled">
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="12" cy="12" r="10" />
                <line x1="15" y1="9" x2="9" y2="15" />
                <line x1="9" y1="9" x2="15" y2="15" />
              </svg>
            </div>
            <div className="stat-content">
              <h3>Đã hủy</h3>
              <p className="stat-value">{statistics?.da_huy || 0}</p>
              <span className="stat-label">Đặt vé đã hủy</span>
            </div>
          </div>
        </div>

        {/* Date Range Filter for Statistics */}
        <div className="date-range-filter">
          <label>Khoảng thời gian thống kê:</label>
          <div className="date-inputs">
            <input
              type="date"
              value={dateRange.tu_ngay}
              onChange={(e) =>
                setDateRange({ ...dateRange, tu_ngay: e.target.value })
              }
            />
            <span>đến</span>
            <input
              type="date"
              value={dateRange.den_ngay}
              onChange={(e) =>
                setDateRange({ ...dateRange, den_ngay: e.target.value })
              }
            />
          </div>
        </div>

        <div className="page-header">
          <div className="header-content">
            <h2>Quản lý Đặt vé</h2>
            <p>Xem và quản lý các đặt vé của khách hàng</p>
          </div>
          <div className="header-actions">
            <div className="search-box">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
              <input
                type="text"
                placeholder="Tìm kiếm mã đặt vé..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="filters-container">
          <div className="filter-group">
            <label>Chuyến bay:</label>
            <select
              value={filterChuyenBay}
              onChange={(e) => setFilterChuyenBay(e.target.value)}
              className="filter-input"
            >
              <option value="">Tất cả</option>
              {flights.map((flight) => (
                <option key={flight.id} value={flight.id}>
                  {flight.ma_chuyen_bay}
                </option>
              ))}
            </select>
          </div>
          <div className="filter-group">
            <label>Trạng thái:</label>
            <select
              value={filterTrangThai}
              onChange={(e) => setFilterTrangThai(e.target.value)}
              className="filter-input"
            >
              <option value="">Tất cả</option>
              <option value="giu_cho">Giữ chỗ</option>
              <option value="da_thanh_toan">Đã thanh toán</option>
              <option value="da_huy">Đã hủy</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Ngày đặt:</label>
            <input
              type="date"
              value={filterNgayDat}
              onChange={(e) => setFilterNgayDat(e.target.value)}
              className="filter-input"
            />
          </div>
          {(filterChuyenBay || filterTrangThai || filterNgayDat) && (
            <button
              className="btn-clear-filters"
              onClick={() => {
                setFilterChuyenBay("");
                setFilterTrangThai("");
                setFilterNgayDat("");
              }}
            >
              Xóa bộ lọc
            </button>
          )}
        </div>

        {loading && bookings.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách đặt vé...</p>
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
            <button className="btn-retry" onClick={loadBookings}>
              Thử lại
            </button>
          </div>
        ) : filteredBookings.length === 0 ? (
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
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
            </div>
            <h3>
              {searchTerm || filterChuyenBay || filterTrangThai || filterNgayDat
                ? "Không tìm thấy đặt vé"
                : "Không có đặt vé nào"}
            </h3>
            <p>
              {searchTerm || filterChuyenBay || filterTrangThai || filterNgayDat
                ? "Thử tìm kiếm với từ khóa khác hoặc xóa bộ lọc"
                : "Chưa có đặt vé nào trong hệ thống"}
            </p>
          </div>
        ) : (
          <>
            <div className="bookings-table-container">
              <table className="bookings-table">
                <thead>
                  <tr>
                    <th>Mã đặt vé</th>
                    <th>Chuyến bay</th>
                    <th>Khách hàng</th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("ngay_dat")}
                    >
                      <div className="th-content">
                        Ngày đặt
                        {getSortIcon("ngay_dat")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("tong_tien")}
                    >
                      <div className="th-content">
                        Tổng tiền
                        {getSortIcon("tong_tien")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("trang_thai")}
                    >
                      <div className="th-content">
                        Trạng thái
                        {getSortIcon("trang_thai")}
                      </div>
                    </th>
                    <th className="th-actions">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedBookings.map((booking) => (
                    <tr key={booking.id}>
                      <td>
                        <span className="booking-code">
                          {booking.ma_dat_ve || booking.id}
                        </span>
                      </td>
                      <td>
                        <div className="flight-info">
                          <div className="flight-code">
                            {booking.chuyen_bay?.ma_chuyen_bay || "N/A"}
                          </div>
                          <div className="flight-route">
                            {booking.chuyen_bay?.tuyen_bay?.san_bay_di
                              ?.ma_san_bay || "N/A"}{" "}
                            →{" "}
                            {booking.chuyen_bay?.tuyen_bay?.san_bay_den
                              ?.ma_san_bay || "N/A"}
                          </div>
                        </div>
                      </td>
                      <td>
                        <div className="customer-info">
                          <div className="customer-name">
                            {booking.khach_hang?.ten ||
                              booking.nguoi_dung?.ten ||
                              "N/A"}
                          </div>
                          <div className="customer-email">
                            {booking.khach_hang?.email ||
                              booking.nguoi_dung?.email ||
                              ""}
                          </div>
                        </div>
                      </td>
                      <td>
                        {formatDate(booking.created_at || booking.ngay_dat)}
                      </td>
                      <td className="price-cell">
                        <span className="price-value">
                          {formatCurrency(booking.tong_tien)}
                        </span>
                      </td>
                      <td>
                        <span
                          className={getTrangThaiBadgeClass(booking.trang_thai)}
                        >
                          {getTrangThaiLabel(booking.trang_thai)}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-view"
                            onClick={() => handleViewDetail(booking.id)}
                            title="Xem chi tiết"
                          >
                            <svg
                              width="14"
                              height="14"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2"
                            >
                              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                              <circle cx="12" cy="12" r="3" />
                            </svg>
                          </button>
                          {booking.trang_thai === "giu_cho" && (
                            <button
                              className="btn-update"
                              onClick={() =>
                                handleUpdateStatus(booking.id, "da_thanh_toan")
                              }
                              title="Xác nhận thanh toán"
                            >
                              <svg
                                width="14"
                                height="14"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                              >
                                <polyline points="20 6 9 17 4 12" />
                              </svg>
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="pagination">
                <div className="pagination-info">
                  Hiển thị {startIndex + 1}-
                  {Math.min(endIndex, sortedBookings.length)} trong tổng số{" "}
                  {sortedBookings.length} đặt vé
                </div>
                <div className="pagination-controls">
                  <button
                    className="pagination-btn"
                    onClick={() => setCurrentPage(1)}
                    disabled={currentPage === 1}
                  >
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <polyline points="11 17 6 12 11 7" />
                      <polyline points="18 17 13 12 18 7" />
                    </svg>
                  </button>
                  <button
                    className="pagination-btn"
                    onClick={() => setCurrentPage(currentPage - 1)}
                    disabled={currentPage === 1}
                  >
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <polyline points="15 18 9 12 15 6" />
                    </svg>
                  </button>

                  {Array.from({ length: totalPages }, (_, i) => i + 1).map(
                    (page) => {
                      if (
                        page === 1 ||
                        page === totalPages ||
                        (page >= currentPage - 2 && page <= currentPage + 2)
                      ) {
                        return (
                          <button
                            key={page}
                            className={`pagination-btn ${
                              currentPage === page ? "active" : ""
                            }`}
                            onClick={() => setCurrentPage(page)}
                          >
                            {page}
                          </button>
                        );
                      } else if (
                        page === currentPage - 3 ||
                        page === currentPage + 3
                      ) {
                        return (
                          <span key={page} className="pagination-ellipsis">
                            ...
                          </span>
                        );
                      }
                      return null;
                    }
                  )}

                  <button
                    className="pagination-btn"
                    onClick={() => setCurrentPage(currentPage + 1)}
                    disabled={currentPage === totalPages}
                  >
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <polyline points="9 18 15 12 9 6" />
                    </svg>
                  </button>
                  <button
                    className="pagination-btn"
                    onClick={() => setCurrentPage(totalPages)}
                    disabled={currentPage === totalPages}
                  >
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <polyline points="13 17 18 12 13 7" />
                      <polyline points="6 17 11 12 6 7" />
                    </svg>
                  </button>
                </div>
              </div>
            )}
          </>
        )}

        {/* Detail Modal */}
        {showDetailModal &&
          selectedBooking &&
          createPortal(
            <div
              className="modal-overlay"
              onClick={() => setShowDetailModal(false)}
            >
              <div
                className="modal-content detail-modal"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="modal-header">
                  <h3>Chi tiết đặt vé</h3>
                  <button
                    className="btn-close"
                    onClick={() => setShowDetailModal(false)}
                  >
                    <svg
                      width="24"
                      height="24"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                  </button>
                </div>

                {loadingDetail ? (
                  <div className="loading-container">
                    <div className="loading-spinner-large"></div>
                    <p>Đang tải chi tiết...</p>
                  </div>
                ) : (
                  <div className="detail-content">
                    <div className="detail-section">
                      <h4>Thông tin đặt vé</h4>
                      <div className="detail-grid">
                        <div className="detail-item">
                          <label>Mã đặt vé:</label>
                          <span>
                            {selectedBooking.ma_dat_ve || selectedBooking.id}
                          </span>
                        </div>
                        <div className="detail-item">
                          <label>Trạng thái:</label>
                          <span
                            className={getTrangThaiBadgeClass(
                              selectedBooking.trang_thai
                            )}
                          >
                            {getTrangThaiLabel(selectedBooking.trang_thai)}
                          </span>
                        </div>
                        <div className="detail-item">
                          <label>Ngày đặt:</label>
                          <span>
                            {formatDate(
                              selectedBooking.created_at ||
                                selectedBooking.ngay_dat
                            )}
                          </span>
                        </div>
                        <div className="detail-item">
                          <label>Tổng tiền:</label>
                          <span className="price-value">
                            {formatCurrency(selectedBooking.tong_tien)}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="detail-section">
                      <h4>Thông tin chuyến bay</h4>
                      <div className="detail-grid">
                        <div className="detail-item">
                          <label>Mã chuyến bay:</label>
                          <span>
                            {selectedBooking.chuyen_bay?.ma_chuyen_bay || "N/A"}
                          </span>
                        </div>
                        <div className="detail-item">
                          <label>Tuyến bay:</label>
                          <span>
                            {selectedBooking.chuyen_bay?.tuyen_bay?.san_bay_di
                              ?.ten_san_bay || "N/A"}{" "}
                            →{" "}
                            {selectedBooking.chuyen_bay?.tuyen_bay?.san_bay_den
                              ?.ten_san_bay || "N/A"}
                          </span>
                        </div>
                        <div className="detail-item">
                          <label>Giờ khởi hành:</label>
                          <span>
                            {formatDate(
                              selectedBooking.chuyen_bay?.gio_khoi_hanh
                            )}
                          </span>
                        </div>
                        <div className="detail-item">
                          <label>Giờ hạ cánh:</label>
                          <span>
                            {formatDate(
                              selectedBooking.chuyen_bay?.gio_ha_canh
                            )}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="detail-section">
                      <h4>Thông tin khách hàng</h4>
                      <div className="detail-grid">
                        <div className="detail-item">
                          <label>Tên:</label>
                          <span>
                            {selectedBooking.khach_hang?.ten ||
                              selectedBooking.nguoi_dung?.ten ||
                              "N/A"}
                          </span>
                        </div>
                        <div className="detail-item">
                          <label>Email:</label>
                          <span>
                            {selectedBooking.khach_hang?.email ||
                              selectedBooking.nguoi_dung?.email ||
                              "N/A"}
                          </span>
                        </div>
                        <div className="detail-item">
                          <label>Số điện thoại:</label>
                          <span>
                            {selectedBooking.khach_hang?.so_dien_thoai ||
                              selectedBooking.nguoi_dung?.so_dien_thoai ||
                              "N/A"}
                          </span>
                        </div>
                      </div>
                    </div>

                    {selectedBooking.hanh_khach &&
                      selectedBooking.hanh_khach.length > 0 && (
                        <div className="detail-section">
                          <h4>
                            Danh sách hành khách (
                            {selectedBooking.hanh_khach.length})
                          </h4>
                          <div className="passengers-list">
                            {selectedBooking.hanh_khach.map(
                              (passenger, index) => (
                                <div key={index} className="passenger-item">
                                  <div className="passenger-info">
                                    <strong>{passenger.ten}</strong>
                                    <span>
                                      {passenger.loai_hanh_khach || "Người lớn"}
                                    </span>
                                  </div>
                                  {passenger.so_cccd && (
                                    <div className="passenger-doc">
                                      CCCD: {passenger.so_cccd}
                                    </div>
                                  )}
                                </div>
                              )
                            )}
                          </div>
                        </div>
                      )}

                    {selectedBooking.trang_thai === "giu_cho" && (
                      <div className="modal-footer">
                        <button
                          type="button"
                          className="btn-cancel"
                          onClick={() => setShowDetailModal(false)}
                        >
                          Đóng
                        </button>
                        <button
                          type="button"
                          className="btn-submit"
                          onClick={() => {
                            handleUpdateStatus(
                              selectedBooking.id,
                              "da_thanh_toan"
                            );
                            setShowDetailModal(false);
                          }}
                        >
                          Xác nhận thanh toán
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>,
            document.body
          )}
      </div>
    </DashboardLayout>
  );
}
