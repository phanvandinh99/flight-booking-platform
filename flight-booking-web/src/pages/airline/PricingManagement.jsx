import React, { useState, useEffect } from "react";
import { createPortal } from "react-dom";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { airlineMenuItems } from "../../config/airlineMenu";
import {
  getPricing,
  createPricing,
  updatePricing,
  deletePricing,
  getFlightsForPricing,
} from "../../api/airline";
import "../../styles/pricingManagement.css";

export default function PricingManagement() {
  const [pricings, setPricings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingPricing, setEditingPricing] = useState(null);
  const [formData, setFormData] = useState({
    ma_chuyen_bay: "",
    hang_ve: "pho_thong",
    gia: "",
    hanh_ly_ky_gui: "",
    chinh_sach_huy_ve: "",
    chinh_sach_doi_ve: "",
    ngay_bat_dau: "",
    ngay_ket_thuc: "",
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDirection, setSortDirection] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);

  // Filter states
  const [filterChuyenBay, setFilterChuyenBay] = useState("");
  const [filterHangVe, setFilterHangVe] = useState("");
  const [filterNgayBatDau, setFilterNgayBatDau] = useState("");

  // Dropdown data
  const [flights, setFlights] = useState([]);
  const [loadingFlights, setLoadingFlights] = useState(false);

  useEffect(() => {
    loadPricings();
    loadFlights();
  }, []);

  const loadPricings = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = {};
      if (filterChuyenBay) params.ma_chuyen_bay = filterChuyenBay;
      if (filterHangVe) params.hang_ve = filterHangVe;
      if (filterNgayBatDau) params.ngay_bat_dau = filterNgayBatDau;

      const response = await getPricing(params);
      setPricings(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Không thể tải danh sách giá vé");
      console.error("Error loading pricings:", err);
    } finally {
      setLoading(false);
    }
  };

  const loadFlights = async () => {
    try {
      setLoadingFlights(true);
      const response = await getFlightsForPricing();
      console.log("Flights response:", response); // Debug log
      // Handle both response.data and direct response
      const flightsData = response?.data || response || [];
      setFlights(Array.isArray(flightsData) ? flightsData : []);
      console.log("Flights loaded:", flightsData.length); // Debug log
    } catch (err) {
      console.error("Error loading flights:", err);
      console.error("Error details:", err.response?.data); // Debug log
      setFlights([]);
    } finally {
      setLoadingFlights(false);
    }
  };

  useEffect(() => {
    loadPricings();
  }, [filterChuyenBay, filterHangVe, filterNgayBatDau]);

  const handleOpenModal = async (pricing = null) => {
    // Reload flights to ensure we have the latest data
    await loadFlights();

    if (pricing) {
      setEditingPricing(pricing);
      // Format dates for input fields
      const ngayBatDau = pricing.ngay_bat_dau
        ? new Date(pricing.ngay_bat_dau).toISOString().split("T")[0]
        : "";
      const ngayKetThuc = pricing.ngay_ket_thuc
        ? new Date(pricing.ngay_ket_thuc).toISOString().split("T")[0]
        : "";

      setFormData({
        ma_chuyen_bay: pricing.ma_chuyen_bay?.toString() || "",
        hang_ve: pricing.hang_ve || "pho_thong",
        gia: pricing.gia?.toString() || "",
        hanh_ly_ky_gui: pricing.hanh_ly_ky_gui || "",
        chinh_sach_huy_ve: pricing.chinh_sach_huy_ve || "",
        chinh_sach_doi_ve: pricing.chinh_sach_doi_ve || "",
        ngay_bat_dau: ngayBatDau,
        ngay_ket_thuc: ngayKetThuc,
      });
    } else {
      setEditingPricing(null);
      setFormData({
        ma_chuyen_bay: "",
        hang_ve: "pho_thong",
        gia: "",
        hanh_ly_ky_gui: "",
        chinh_sach_huy_ve: "",
        chinh_sach_doi_ve: "",
        ngay_bat_dau: "",
        ngay_ket_thuc: "",
      });
    }
    setFormErrors({});
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingPricing(null);
    setFormData({
      ma_chuyen_bay: "",
      hang_ve: "pho_thong",
      gia: "",
      hanh_ly_ky_gui: "",
      chinh_sach_huy_ve: "",
      chinh_sach_doi_ve: "",
      ngay_bat_dau: "",
      ngay_ket_thuc: "",
    });
    setFormErrors({});
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.ma_chuyen_bay) {
      errors.ma_chuyen_bay = "Chuyến bay là bắt buộc";
    }
    if (!formData.hang_ve) {
      errors.hang_ve = "Hạng vé là bắt buộc";
    }
    if (!formData.gia || parseFloat(formData.gia) <= 0) {
      errors.gia = "Giá vé phải lớn hơn 0";
    }
    if (!formData.ngay_bat_dau) {
      errors.ngay_bat_dau = "Ngày bắt đầu là bắt buộc";
    }
    if (!formData.ngay_ket_thuc) {
      errors.ngay_ket_thuc = "Ngày kết thúc là bắt buộc";
    }
    if (formData.ngay_bat_dau && formData.ngay_ket_thuc) {
      const batDau = new Date(formData.ngay_bat_dau);
      const ketThuc = new Date(formData.ngay_ket_thuc);
      if (ketThuc < batDau) {
        errors.ngay_ket_thuc = "Ngày kết thúc phải sau ngày bắt đầu";
      }
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      setSubmitting(true);

      const submitData = {
        ma_chuyen_bay: parseInt(formData.ma_chuyen_bay),
        hang_ve: formData.hang_ve,
        gia: parseFloat(formData.gia),
        hanh_ly_ky_gui: formData.hanh_ly_ky_gui || "",
        chinh_sach_huy_ve: formData.chinh_sach_huy_ve || "",
        chinh_sach_doi_ve: formData.chinh_sach_doi_ve || "",
        ngay_bat_dau: formData.ngay_bat_dau,
        ngay_ket_thuc: formData.ngay_ket_thuc,
      };

      if (editingPricing) {
        await updatePricing(editingPricing.id, submitData);
      } else {
        await createPricing(submitData);
      }
      await loadPricings();
      handleCloseModal();
    } catch (err) {
      if (err.response?.data?.errors) {
        setFormErrors(err.response.data.errors);
      } else {
        alert(err.response?.data?.message || "Có lỗi xảy ra");
      }
      console.error("Error saving pricing:", err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa giá vé này?")) {
      return;
    }

    try {
      await deletePricing(id);
      await loadPricings();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể xóa giá vé");
      console.error("Error deleting pricing:", err);
    }
  };

  const filteredPricings = pricings.filter((pricing) => {
    const search = searchTerm.toLowerCase();
    return (
      pricing.chuyen_bay?.ma_chuyen_bay?.toLowerCase().includes(search) ||
      pricing.chuyen_bay?.tuyen_bay?.san_bay_di?.ten_san_bay
        ?.toLowerCase()
        .includes(search) ||
      pricing.chuyen_bay?.tuyen_bay?.san_bay_den?.ten_san_bay
        ?.toLowerCase()
        .includes(search) ||
      pricing.gia?.toString().includes(search)
    );
  });

  // Sort pricings
  const sortedPricings = [...filteredPricings].sort((a, b) => {
    if (!sortField) return 0;

    let aValue, bValue;
    if (sortField === "gia") {
      aValue = parseFloat(a.gia || 0);
      bValue = parseFloat(b.gia || 0);
    } else if (sortField === "ngay_bat_dau") {
      aValue = new Date(a.ngay_bat_dau || 0).getTime();
      bValue = new Date(b.ngay_bat_dau || 0).getTime();
    } else if (sortField === "hang_ve") {
      aValue = a.hang_ve?.toLowerCase() || "";
      bValue = b.hang_ve?.toLowerCase() || "";
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
  const totalPages = Math.ceil(sortedPricings.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedPricings = sortedPricings.slice(startIndex, endIndex);

  // Reset to page 1 when search or sort changes
  useEffect(() => {
    setCurrentPage(1);
  }, [
    searchTerm,
    sortField,
    sortDirection,
    filterChuyenBay,
    filterHangVe,
    filterNgayBatDau,
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

  const getHangVeLabel = (hangVe) => {
    const labels = {
      pho_thong: "Phổ thông",
      thuong_gia: "Thương gia",
      hang_nhat: "Hạng nhất",
    };
    return labels[hangVe] || hangVe;
  };

  const getHangVeBadgeClass = (hangVe) => {
    const classes = {
      pho_thong: "fare-badge fare-economy",
      thuong_gia: "fare-badge fare-business",
      hang_nhat: "fare-badge fare-first",
    };
    return classes[hangVe] || "fare-badge";
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
    });
  };

  return (
    <DashboardLayout menuItems={airlineMenuItems} title="Quản lý Giá Vé">
      <div className="pricing-management-page">
        <div className="page-header">
          <div className="header-content">
            <h2>Quản lý Giá Vé</h2>
            <p>Cập nhật giá vé theo ngày, hạng ghế và chính sách khuyến mãi</p>
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
                placeholder="Tìm kiếm giá vé..."
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
              Thêm giá vé
            </button>
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
              disabled={loadingFlights}
            >
              <option value="">Tất cả</option>
              {loadingFlights && (
                <option value="" disabled>
                  Đang tải...
                </option>
              )}
              {flights.map((flight) => (
                <option key={flight.id} value={flight.id}>
                  {flight.ma_chuyen_bay}
                </option>
              ))}
            </select>
          </div>
          <div className="filter-group">
            <label>Hạng vé:</label>
            <select
              value={filterHangVe}
              onChange={(e) => setFilterHangVe(e.target.value)}
              className="filter-input"
            >
              <option value="">Tất cả</option>
              <option value="pho_thong">Phổ thông</option>
              <option value="thuong_gia">Thương gia</option>
              <option value="hang_nhat">Hạng nhất</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Ngày bắt đầu:</label>
            <input
              type="date"
              value={filterNgayBatDau}
              onChange={(e) => setFilterNgayBatDau(e.target.value)}
              className="filter-input"
            />
          </div>
          {(filterChuyenBay || filterHangVe || filterNgayBatDau) && (
            <button
              className="btn-clear-filters"
              onClick={() => {
                setFilterChuyenBay("");
                setFilterHangVe("");
                setFilterNgayBatDau("");
              }}
            >
              Xóa bộ lọc
            </button>
          )}
        </div>

        {loading && pricings.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách giá vé...</p>
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
            <button className="btn-retry" onClick={loadPricings}>
              Thử lại
            </button>
          </div>
        ) : filteredPricings.length === 0 ? (
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
                <line x1="12" y1="1" x2="12" y2="23" />
                <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
              </svg>
            </div>
            <h3>
              {searchTerm || filterChuyenBay || filterHangVe || filterNgayBatDau
                ? "Không tìm thấy giá vé"
                : "Không có giá vé nào"}
            </h3>
            <p>
              {searchTerm || filterChuyenBay || filterHangVe || filterNgayBatDau
                ? "Thử tìm kiếm với từ khóa khác hoặc xóa bộ lọc"
                : "Bắt đầu bằng cách thêm giá vé mới"}
            </p>
            {!searchTerm &&
              !filterChuyenBay &&
              !filterHangVe &&
              !filterNgayBatDau && (
                <button
                  className="btn-primary"
                  onClick={() => handleOpenModal()}
                >
                  Thêm giá vé đầu tiên
                </button>
              )}
          </div>
        ) : (
          <>
            <div className="pricings-table-container">
              <table className="pricings-table">
                <thead>
                  <tr>
                    <th>Chuyến bay</th>
                    <th>Tuyến bay</th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("hang_ve")}
                    >
                      <div className="th-content">
                        Hạng vé
                        {getSortIcon("hang_ve")}
                      </div>
                    </th>
                    <th className="sortable" onClick={() => handleSort("gia")}>
                      <div className="th-content">
                        Giá vé
                        {getSortIcon("gia")}
                      </div>
                    </th>
                    <th>Hành lý ký gửi</th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("ngay_bat_dau")}
                    >
                      <div className="th-content">
                        Ngày bắt đầu
                        {getSortIcon("ngay_bat_dau")}
                      </div>
                    </th>
                    <th>Ngày kết thúc</th>
                    <th className="th-actions">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedPricings.map((pricing) => (
                    <tr key={pricing.id}>
                      <td>
                        <span className="flight-code">
                          {pricing.chuyen_bay?.ma_chuyen_bay || "N/A"}
                        </span>
                      </td>
                      <td>
                        <div className="route-info">
                          <div className="route-route">
                            {pricing.chuyen_bay?.tuyen_bay?.san_bay_di
                              ?.ma_san_bay || "N/A"}{" "}
                            →{" "}
                            {pricing.chuyen_bay?.tuyen_bay?.san_bay_den
                              ?.ma_san_bay || "N/A"}
                          </div>
                          <div className="route-airports">
                            {pricing.chuyen_bay?.tuyen_bay?.san_bay_di
                              ?.ten_san_bay || "N/A"}{" "}
                            -{" "}
                            {pricing.chuyen_bay?.tuyen_bay?.san_bay_den
                              ?.ten_san_bay || "N/A"}
                          </div>
                        </div>
                      </td>
                      <td>
                        <span className={getHangVeBadgeClass(pricing.hang_ve)}>
                          {getHangVeLabel(pricing.hang_ve)}
                        </span>
                      </td>
                      <td className="price-cell">
                        <span className="price-value">
                          {formatCurrency(pricing.gia)}
                        </span>
                      </td>
                      <td>{pricing.hanh_ly_ky_gui || "N/A"}</td>
                      <td>{formatDate(pricing.ngay_bat_dau)}</td>
                      <td>{formatDate(pricing.ngay_ket_thuc)}</td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-edit"
                            onClick={() => handleOpenModal(pricing)}
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
                            onClick={() => handleDelete(pricing.id)}
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
                  {Math.min(endIndex, sortedPricings.length)} trong tổng số{" "}
                  {sortedPricings.length} giá vé
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
                  <h3>{editingPricing ? "Sửa giá vé" : "Thêm giá vé mới"}</h3>
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
                  <div className="form-group">
                    <label htmlFor="ma_chuyen_bay">
                      Chuyến bay <span className="required">*</span>
                    </label>
                    <select
                      id="ma_chuyen_bay"
                      value={formData.ma_chuyen_bay}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          ma_chuyen_bay: e.target.value,
                        })
                      }
                      className={formErrors.ma_chuyen_bay ? "error" : ""}
                      disabled={submitting || loadingFlights}
                    >
                      <option value="">
                        {loadingFlights
                          ? "Đang tải chuyến bay..."
                          : "Chọn chuyến bay"}
                      </option>
                      {flights.length === 0 && !loadingFlights && (
                        <option value="" disabled>
                          Không có chuyến bay nào
                        </option>
                      )}
                      {flights.map((flight) => (
                        <option key={flight.id} value={flight.id}>
                          {flight.ma_chuyen_bay} -{" "}
                          {flight.tuyen_bay?.san_bay_di?.ma_san_bay || "N/A"} →{" "}
                          {flight.tuyen_bay?.san_bay_den?.ma_san_bay || "N/A"}
                        </option>
                      ))}
                    </select>
                    {formErrors.ma_chuyen_bay && (
                      <span className="error-message">
                        {formErrors.ma_chuyen_bay}
                      </span>
                    )}
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="hang_ve">
                        Hạng vé <span className="required">*</span>
                      </label>
                      <select
                        id="hang_ve"
                        value={formData.hang_ve}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            hang_ve: e.target.value,
                          })
                        }
                        className={formErrors.hang_ve ? "error" : ""}
                        disabled={submitting}
                      >
                        <option value="pho_thong">Phổ thông</option>
                        <option value="thuong_gia">Thương gia</option>
                        <option value="hang_nhat">Hạng nhất</option>
                      </select>
                      {formErrors.hang_ve && (
                        <span className="error-message">
                          {formErrors.hang_ve}
                        </span>
                      )}
                    </div>

                    <div className="form-group">
                      <label htmlFor="gia">
                        Giá vé (VND) <span className="required">*</span>
                      </label>
                      <input
                        id="gia"
                        type="number"
                        min="0"
                        step="1000"
                        value={formData.gia}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            gia: e.target.value,
                          })
                        }
                        className={formErrors.gia ? "error" : ""}
                        placeholder="VD: 1500000"
                        disabled={submitting}
                      />
                      {formErrors.gia && (
                        <span className="error-message">{formErrors.gia}</span>
                      )}
                    </div>
                  </div>

                  <div className="form-group">
                    <label htmlFor="hanh_ly_ky_gui">Hành lý ký gửi</label>
                    <input
                      id="hanh_ly_ky_gui"
                      type="text"
                      value={formData.hanh_ly_ky_gui}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          hanh_ly_ky_gui: e.target.value,
                        })
                      }
                      className={formErrors.hanh_ly_ky_gui ? "error" : ""}
                      placeholder="VD: 20kg"
                      disabled={submitting}
                    />
                    {formErrors.hanh_ly_ky_gui && (
                      <span className="error-message">
                        {formErrors.hanh_ly_ky_gui}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="chinh_sach_huy_ve">Chính sách hủy vé</label>
                    <textarea
                      id="chinh_sach_huy_ve"
                      value={formData.chinh_sach_huy_ve}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          chinh_sach_huy_ve: e.target.value,
                        })
                      }
                      className={formErrors.chinh_sach_huy_ve ? "error" : ""}
                      placeholder="VD: Hủy trước 24h: hoàn 100%"
                      rows="3"
                      disabled={submitting}
                    />
                    {formErrors.chinh_sach_huy_ve && (
                      <span className="error-message">
                        {formErrors.chinh_sach_huy_ve}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="chinh_sach_doi_ve">Chính sách đổi vé</label>
                    <textarea
                      id="chinh_sach_doi_ve"
                      value={formData.chinh_sach_doi_ve}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          chinh_sach_doi_ve: e.target.value,
                        })
                      }
                      className={formErrors.chinh_sach_doi_ve ? "error" : ""}
                      placeholder="VD: Đổi trước 2h: phí 200k"
                      rows="3"
                      disabled={submitting}
                    />
                    {formErrors.chinh_sach_doi_ve && (
                      <span className="error-message">
                        {formErrors.chinh_sach_doi_ve}
                      </span>
                    )}
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="ngay_bat_dau">
                        Ngày bắt đầu <span className="required">*</span>
                      </label>
                      <input
                        id="ngay_bat_dau"
                        type="date"
                        value={formData.ngay_bat_dau}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            ngay_bat_dau: e.target.value,
                          })
                        }
                        className={formErrors.ngay_bat_dau ? "error" : ""}
                        disabled={submitting}
                      />
                      {formErrors.ngay_bat_dau && (
                        <span className="error-message">
                          {formErrors.ngay_bat_dau}
                        </span>
                      )}
                    </div>

                    <div className="form-group">
                      <label htmlFor="ngay_ket_thuc">
                        Ngày kết thúc <span className="required">*</span>
                      </label>
                      <input
                        id="ngay_ket_thuc"
                        type="date"
                        value={formData.ngay_ket_thuc}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            ngay_ket_thuc: e.target.value,
                          })
                        }
                        className={formErrors.ngay_ket_thuc ? "error" : ""}
                        disabled={submitting}
                      />
                      {formErrors.ngay_ket_thuc && (
                        <span className="error-message">
                          {formErrors.ngay_ket_thuc}
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
                      ) : editingPricing ? (
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
