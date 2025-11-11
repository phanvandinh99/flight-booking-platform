import React, { useState, useEffect } from "react";
import { createPortal } from "react-dom";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { airlineMenuItems } from "../../config/airlineMenu";
import {
  getFlights,
  createFlight,
  updateFlight,
  deleteFlight,
  getApprovedRoutes,
} from "../../api/airline";
import { getAircrafts } from "../../api/airline";
import "../../styles/flightManagement.css";

export default function FlightManagement() {
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingFlight, setEditingFlight] = useState(null);
  const [formData, setFormData] = useState({
    ma_may_bay: "",
    ma_chuyen_bay: "",
    ma_tuyen_bay: "",
    gio_khoi_hanh: "",
    gio_ha_canh: "",
    tan_suat: "hang_ngay",
    trang_thai: "du_kien",
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDirection, setSortDirection] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);

  // Filter states
  const [filterNgayKhoiHanh, setFilterNgayKhoiHanh] = useState("");
  const [filterTrangThai, setFilterTrangThai] = useState("");
  const [filterTuyenBay, setFilterTuyenBay] = useState("");

  // Dropdown data
  const [aircrafts, setAircrafts] = useState([]);
  const [approvedRoutes, setApprovedRoutes] = useState([]);

  useEffect(() => {
    loadFlights();
    loadAircrafts();
    loadApprovedRoutes();
  }, []);

  const loadFlights = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = {};
      if (filterNgayKhoiHanh) params.ngay_khoi_hanh = filterNgayKhoiHanh;
      if (filterTrangThai) params.trang_thai = filterTrangThai;
      if (filterTuyenBay) params.ma_tuyen_bay = filterTuyenBay;

      const response = await getFlights(params);
      setFlights(response.data || []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Không thể tải danh sách chuyến bay"
      );
      console.error("Error loading flights:", err);
    } finally {
      setLoading(false);
    }
  };

  const loadAircrafts = async () => {
    try {
      const response = await getAircrafts();
      setAircrafts(response.data || []);
    } catch (err) {
      console.error("Error loading aircrafts:", err);
    }
  };

  const loadApprovedRoutes = async () => {
    try {
      const response = await getApprovedRoutes();
      setApprovedRoutes(response.data || []);
    } catch (err) {
      console.error("Error loading approved routes:", err);
    }
  };

  useEffect(() => {
    loadFlights();
  }, [filterNgayKhoiHanh, filterTrangThai, filterTuyenBay]);

  const handleOpenModal = (flight = null) => {
    if (flight) {
      setEditingFlight(flight);
      // Format datetime for input fields
      const gioKhoiHanh = flight.gio_khoi_hanh
        ? new Date(flight.gio_khoi_hanh).toISOString().slice(0, 16)
        : "";
      const gioHaCanh = flight.gio_ha_canh
        ? new Date(flight.gio_ha_canh).toISOString().slice(0, 16)
        : "";

      setFormData({
        ma_may_bay: flight.ma_may_bay?.toString() || "",
        ma_chuyen_bay: flight.ma_chuyen_bay || "",
        ma_tuyen_bay: flight.ma_tuyen_bay?.toString() || "",
        gio_khoi_hanh: gioKhoiHanh,
        gio_ha_canh: gioHaCanh,
        tan_suat: flight.tan_suat || "hang_ngay",
        trang_thai: flight.trang_thai || "du_kien",
      });
    } else {
      setEditingFlight(null);
      setFormData({
        ma_may_bay: "",
        ma_chuyen_bay: "",
        ma_tuyen_bay: "",
        gio_khoi_hanh: "",
        gio_ha_canh: "",
        tan_suat: "hang_ngay",
        trang_thai: "du_kien",
      });
    }
    setFormErrors({});
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingFlight(null);
    setFormData({
      ma_may_bay: "",
      ma_chuyen_bay: "",
      ma_tuyen_bay: "",
      gio_khoi_hanh: "",
      gio_ha_canh: "",
      tan_suat: "hang_ngay",
      trang_thai: "du_kien",
    });
    setFormErrors({});
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.ma_may_bay) {
      errors.ma_may_bay = "Máy bay là bắt buộc";
    }
    if (!formData.ma_chuyen_bay.trim()) {
      errors.ma_chuyen_bay = "Mã chuyến bay là bắt buộc";
    }
    if (!formData.ma_tuyen_bay) {
      errors.ma_tuyen_bay = "Tuyến bay là bắt buộc";
    }
    if (!formData.gio_khoi_hanh) {
      errors.gio_khoi_hanh = "Giờ khởi hành là bắt buộc";
    }
    if (!formData.gio_ha_canh) {
      errors.gio_ha_canh = "Giờ hạ cánh là bắt buộc";
    }
    if (formData.gio_khoi_hanh && formData.gio_ha_canh) {
      const khoiHanh = new Date(formData.gio_khoi_hanh);
      const haCanh = new Date(formData.gio_ha_canh);
      if (haCanh <= khoiHanh) {
        errors.gio_ha_canh = "Giờ hạ cánh phải sau giờ khởi hành";
      }
    }
    if (!formData.tan_suat) {
      errors.tan_suat = "Tần suất là bắt buộc";
    }
    if (!formData.trang_thai) {
      errors.trang_thai = "Trạng thái là bắt buộc";
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      setSubmitting(true);

      // Format datetime to ISO string
      const submitData = {
        ...formData,
        ma_may_bay: parseInt(formData.ma_may_bay),
        ma_tuyen_bay: parseInt(formData.ma_tuyen_bay),
        gio_khoi_hanh: new Date(formData.gio_khoi_hanh).toISOString(),
        gio_ha_canh: new Date(formData.gio_ha_canh).toISOString(),
      };

      if (editingFlight) {
        await updateFlight(editingFlight.id, submitData);
      } else {
        await createFlight(submitData);
      }
      await loadFlights();
      handleCloseModal();
    } catch (err) {
      if (err.response?.data?.errors) {
        setFormErrors(err.response.data.errors);
      } else {
        alert(err.response?.data?.message || "Có lỗi xảy ra");
      }
      console.error("Error saving flight:", err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa chuyến bay này?")) {
      return;
    }

    try {
      await deleteFlight(id);
      await loadFlights();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể xóa chuyến bay");
      console.error("Error deleting flight:", err);
    }
  };

  const filteredFlights = flights.filter((flight) => {
    const search = searchTerm.toLowerCase();
    return (
      flight.ma_chuyen_bay?.toLowerCase().includes(search) ||
      flight.may_bay?.loai_may_bay?.toLowerCase().includes(search) ||
      flight.tuyen_bay?.san_bay_di?.ten_san_bay
        ?.toLowerCase()
        .includes(search) ||
      flight.tuyen_bay?.san_bay_den?.ten_san_bay?.toLowerCase().includes(search)
    );
  });

  // Sort flights
  const sortedFlights = [...filteredFlights].sort((a, b) => {
    if (!sortField) return 0;

    let aValue, bValue;
    if (sortField === "ma_chuyen_bay") {
      aValue = a.ma_chuyen_bay?.toLowerCase() || "";
      bValue = b.ma_chuyen_bay?.toLowerCase() || "";
    } else if (sortField === "gio_khoi_hanh") {
      aValue = new Date(a.gio_khoi_hanh || 0).getTime();
      bValue = new Date(b.gio_khoi_hanh || 0).getTime();
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
  const totalPages = Math.ceil(sortedFlights.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedFlights = sortedFlights.slice(startIndex, endIndex);

  // Reset to page 1 when search or sort changes
  useEffect(() => {
    setCurrentPage(1);
  }, [
    searchTerm,
    sortField,
    sortDirection,
    filterNgayKhoiHanh,
    filterTrangThai,
    filterTuyenBay,
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
      du_kien: "Dự kiến",
      bi_huy: "Bị hủy",
      da_hoan_thanh: "Đã hoàn thành",
    };
    return labels[trangThai] || trangThai;
  };

  const getTrangThaiBadgeClass = (trangThai) => {
    const classes = {
      du_kien: "status-badge status-scheduled",
      bi_huy: "status-badge status-cancelled",
      da_hoan_thanh: "status-badge status-completed",
    };
    return classes[trangThai] || "status-badge";
  };

  const getTanSuatLabel = (tanSuat) => {
    const labels = {
      hang_ngay: "Hàng ngày",
      thu_2: "Thứ 2",
      thu_3: "Thứ 3",
      thu_4: "Thứ 4",
      thu_5: "Thứ 5",
      thu_6: "Thứ 6",
      thu_7: "Thứ 7",
      chu_nhat: "Chủ nhật",
    };
    return labels[tanSuat] || tanSuat;
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <DashboardLayout menuItems={airlineMenuItems} title="Quản lý Chuyến Bay">
      <div className="flight-management-page">
        <div className="page-header">
          <div className="header-content">
            <h2>Quản lý Chuyến Bay</h2>
            <p>Tạo và quản lý các chuyến bay</p>
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
                placeholder="Tìm kiếm chuyến bay..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <button className="btn-primary" onClick={() => handleOpenModal()}>
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              Thêm chuyến bay
            </button>
          </div>
        </div>

        {/* Filters */}
        <div className="filters-container">
          <div className="filter-group">
            <label>Ngày khởi hành:</label>
            <input
              type="date"
              value={filterNgayKhoiHanh}
              onChange={(e) => setFilterNgayKhoiHanh(e.target.value)}
              className="filter-input"
            />
          </div>
          <div className="filter-group">
            <label>Trạng thái:</label>
            <select
              value={filterTrangThai}
              onChange={(e) => setFilterTrangThai(e.target.value)}
              className="filter-input"
            >
              <option value="">Tất cả</option>
              <option value="du_kien">Dự kiến</option>
              <option value="bi_huy">Bị hủy</option>
              <option value="da_hoan_thanh">Đã hoàn thành</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Tuyến bay:</label>
            <select
              value={filterTuyenBay}
              onChange={(e) => setFilterTuyenBay(e.target.value)}
              className="filter-input"
            >
              <option value="">Tất cả</option>
              {approvedRoutes.map((route) => (
                <option key={route.id} value={route.id}>
                  {route.san_bay_di?.ma_san_bay} -{" "}
                  {route.san_bay_den?.ma_san_bay}
                </option>
              ))}
            </select>
          </div>
          {(filterNgayKhoiHanh || filterTrangThai || filterTuyenBay) && (
            <button
              className="btn-clear-filters"
              onClick={() => {
                setFilterNgayKhoiHanh("");
                setFilterTrangThai("");
                setFilterTuyenBay("");
              }}
            >
              Xóa bộ lọc
            </button>
          )}
        </div>

        {loading && flights.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách chuyến bay...</p>
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
            <button className="btn-retry" onClick={loadFlights}>
              Thử lại
            </button>
          </div>
        ) : filteredFlights.length === 0 ? (
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
                <path d="M21 16V14C21 12.9 20.1 12 19 12H5C3.9 12 3 12.9 3 14V16C3 17.1 3.9 18 5 18H19C20.1 18 21 17.1 21 16Z" />
                <path d="M7 10L12 15L17 10" />
              </svg>
            </div>
            <h3>
              {searchTerm ||
              filterNgayKhoiHanh ||
              filterTrangThai ||
              filterTuyenBay
                ? "Không tìm thấy chuyến bay"
                : "Không có chuyến bay nào"}
            </h3>
            <p>
              {searchTerm ||
              filterNgayKhoiHanh ||
              filterTrangThai ||
              filterTuyenBay
                ? "Thử tìm kiếm với từ khóa khác hoặc xóa bộ lọc"
                : "Bắt đầu bằng cách thêm chuyến bay mới"}
            </p>
            {!searchTerm &&
              !filterNgayKhoiHanh &&
              !filterTrangThai &&
              !filterTuyenBay && (
                <button
                  className="btn-primary"
                  onClick={() => handleOpenModal()}
                >
                  Thêm chuyến bay đầu tiên
                </button>
              )}
          </div>
        ) : (
          <>
            <div className="flights-table-container">
              <table className="flights-table">
                <thead>
                  <tr>
                    <th
                      className="sortable"
                      onClick={() => handleSort("ma_chuyen_bay")}
                    >
                      <div className="th-content">
                        Mã chuyến bay
                        {getSortIcon("ma_chuyen_bay")}
                      </div>
                    </th>
                    <th>Tuyến bay</th>
                    <th>Máy bay</th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("gio_khoi_hanh")}
                    >
                      <div className="th-content">
                        Giờ khởi hành
                        {getSortIcon("gio_khoi_hanh")}
                      </div>
                    </th>
                    <th>Giờ hạ cánh</th>
                    <th>Tần suất</th>
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
                  {paginatedFlights.map((flight) => (
                    <tr key={flight.id}>
                      <td>
                        <span className="flight-code">
                          {flight.ma_chuyen_bay}
                        </span>
                      </td>
                      <td>
                        <div className="route-info">
                          <div className="route-route">
                            {flight.tuyen_bay?.san_bay_di?.ma_san_bay || "N/A"}{" "}
                            →{" "}
                            {flight.tuyen_bay?.san_bay_den?.ma_san_bay || "N/A"}
                          </div>
                          <div className="route-airports">
                            {flight.tuyen_bay?.san_bay_di?.ten_san_bay || "N/A"}{" "}
                            -{" "}
                            {flight.tuyen_bay?.san_bay_den?.ten_san_bay ||
                              "N/A"}
                          </div>
                        </div>
                      </td>
                      <td>{flight.may_bay?.loai_may_bay || "N/A"}</td>
                      <td>{formatDateTime(flight.gio_khoi_hanh)}</td>
                      <td>{formatDateTime(flight.gio_ha_canh)}</td>
                      <td>{getTanSuatLabel(flight.tan_suat)}</td>
                      <td>
                        <span
                          className={getTrangThaiBadgeClass(flight.trang_thai)}
                        >
                          {getTrangThaiLabel(flight.trang_thai)}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-edit"
                            onClick={() => handleOpenModal(flight)}
                            title="Sửa"
                          >
                            <svg
                              width="11"
                              height="11"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2.5"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            >
                              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                            </svg>
                          </button>
                          <button
                            className="btn-delete"
                            onClick={() => handleDelete(flight.id)}
                            title="Xóa"
                          >
                            <svg
                              width="11"
                              height="11"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2.5"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            >
                              <polyline points="3 6 5 6 21 6" />
                              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                              <line x1="10" y1="11" x2="10" y2="17" />
                              <line x1="14" y1="11" x2="14" y2="17" />
                            </svg>
                          </button>
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
                  {Math.min(endIndex, sortedFlights.length)} trong tổng số{" "}
                  {sortedFlights.length} chuyến bay
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

        {/* Modal Form */}
        {showModal &&
          createPortal(
            <div className="modal-overlay" onClick={handleCloseModal}>
              <div
                className="modal-content"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="modal-header">
                  <h3>
                    {editingFlight ? "Sửa chuyến bay" : "Thêm chuyến bay mới"}
                  </h3>
                  <button className="btn-close" onClick={handleCloseModal}>
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

                <form onSubmit={handleSubmit} className="modal-form">
                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="ma_may_bay">
                        Máy bay <span className="required">*</span>
                      </label>
                      <select
                        id="ma_may_bay"
                        value={formData.ma_may_bay}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            ma_may_bay: e.target.value,
                          })
                        }
                        className={formErrors.ma_may_bay ? "error" : ""}
                        disabled={submitting}
                      >
                        <option value="">Chọn máy bay</option>
                        {aircrafts.map((aircraft) => (
                          <option key={aircraft.id} value={aircraft.id}>
                            {aircraft.loai_may_bay} ({aircraft.tong_so_ghe} ghế)
                          </option>
                        ))}
                      </select>
                      {formErrors.ma_may_bay && (
                        <span className="error-message">
                          {formErrors.ma_may_bay}
                        </span>
                      )}
                    </div>

                    <div className="form-group">
                      <label htmlFor="ma_chuyen_bay">
                        Mã chuyến bay <span className="required">*</span>
                      </label>
                      <input
                        id="ma_chuyen_bay"
                        type="text"
                        value={formData.ma_chuyen_bay}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            ma_chuyen_bay: e.target.value.toUpperCase(),
                          })
                        }
                        className={formErrors.ma_chuyen_bay ? "error" : ""}
                        placeholder="VD: VN123"
                        disabled={submitting}
                      />
                      {formErrors.ma_chuyen_bay && (
                        <span className="error-message">
                          {formErrors.ma_chuyen_bay}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="form-group">
                    <label htmlFor="ma_tuyen_bay">
                      Tuyến bay <span className="required">*</span>
                    </label>
                    <select
                      id="ma_tuyen_bay"
                      value={formData.ma_tuyen_bay}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          ma_tuyen_bay: e.target.value,
                        })
                      }
                      className={formErrors.ma_tuyen_bay ? "error" : ""}
                      disabled={submitting}
                    >
                      <option value="">Chọn tuyến bay</option>
                      {approvedRoutes.map((route) => (
                        <option key={route.id} value={route.id}>
                          {route.san_bay_di?.ma_san_bay} (
                          {route.san_bay_di?.ten_san_bay}) →{" "}
                          {route.san_bay_den?.ma_san_bay} (
                          {route.san_bay_den?.ten_san_bay})
                        </option>
                      ))}
                    </select>
                    {formErrors.ma_tuyen_bay && (
                      <span className="error-message">
                        {formErrors.ma_tuyen_bay}
                      </span>
                    )}
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="gio_khoi_hanh">
                        Giờ khởi hành <span className="required">*</span>
                      </label>
                      <input
                        id="gio_khoi_hanh"
                        type="datetime-local"
                        value={formData.gio_khoi_hanh}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            gio_khoi_hanh: e.target.value,
                          })
                        }
                        className={formErrors.gio_khoi_hanh ? "error" : ""}
                        disabled={submitting}
                      />
                      {formErrors.gio_khoi_hanh && (
                        <span className="error-message">
                          {formErrors.gio_khoi_hanh}
                        </span>
                      )}
                    </div>

                    <div className="form-group">
                      <label htmlFor="gio_ha_canh">
                        Giờ hạ cánh <span className="required">*</span>
                      </label>
                      <input
                        id="gio_ha_canh"
                        type="datetime-local"
                        value={formData.gio_ha_canh}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            gio_ha_canh: e.target.value,
                          })
                        }
                        className={formErrors.gio_ha_canh ? "error" : ""}
                        disabled={submitting}
                      />
                      {formErrors.gio_ha_canh && (
                        <span className="error-message">
                          {formErrors.gio_ha_canh}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="tan_suat">
                        Tần suất <span className="required">*</span>
                      </label>
                      <select
                        id="tan_suat"
                        value={formData.tan_suat}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            tan_suat: e.target.value,
                          })
                        }
                        className={formErrors.tan_suat ? "error" : ""}
                        disabled={submitting}
                      >
                        <option value="hang_ngay">Hàng ngày</option>
                        <option value="thu_2">Thứ 2</option>
                        <option value="thu_3">Thứ 3</option>
                        <option value="thu_4">Thứ 4</option>
                        <option value="thu_5">Thứ 5</option>
                        <option value="thu_6">Thứ 6</option>
                        <option value="thu_7">Thứ 7</option>
                        <option value="chu_nhat">Chủ nhật</option>
                      </select>
                      {formErrors.tan_suat && (
                        <span className="error-message">
                          {formErrors.tan_suat}
                        </span>
                      )}
                    </div>

                    <div className="form-group">
                      <label htmlFor="trang_thai">
                        Trạng thái <span className="required">*</span>
                      </label>
                      <select
                        id="trang_thai"
                        value={formData.trang_thai}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            trang_thai: e.target.value,
                          })
                        }
                        className={formErrors.trang_thai ? "error" : ""}
                        disabled={submitting}
                      >
                        <option value="du_kien">Dự kiến</option>
                        <option value="bi_huy">Bị hủy</option>
                        <option value="da_hoan_thanh">Đã hoàn thành</option>
                      </select>
                      {formErrors.trang_thai && (
                        <span className="error-message">
                          {formErrors.trang_thai}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="modal-footer">
                    <button
                      type="button"
                      className="btn-cancel"
                      onClick={handleCloseModal}
                      disabled={submitting}
                    >
                      Hủy
                    </button>
                    <button
                      type="submit"
                      className="btn-submit"
                      disabled={submitting}
                    >
                      {submitting ? (
                        <>
                          <span className="loading-spinner"></span>
                          Đang lưu...
                        </>
                      ) : editingFlight ? (
                        "Cập nhật"
                      ) : (
                        "Tạo mới"
                      )}
                    </button>
                  </div>
                </form>
              </div>
            </div>,
            document.body
          )}
      </div>
    </DashboardLayout>
  );
}
