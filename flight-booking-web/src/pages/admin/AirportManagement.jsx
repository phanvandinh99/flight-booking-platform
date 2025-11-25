import React, { useState, useEffect } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { adminMenuItems } from "../../config/adminMenu";
import {
  getAirports,
  createAirport,
  updateAirport,
  deleteAirport,
} from "../../api/admin";
import "../../styles/airportManagement.css";

export default function AirportManagement() {
  const [airports, setAirports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingAirport, setEditingAirport] = useState(null);
  const [formData, setFormData] = useState({
    ma_san_bay: "",
    ten_san_bay: "",
    thanh_pho: "",
    quoc_gia: "",
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDirection, setSortDirection] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);

  useEffect(() => {
    loadAirports();
  }, []);

  const loadAirports = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getAirports();
      setAirports(response.data || []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Không thể tải danh sách sân bay"
      );
      console.error("Error loading airports:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (airport = null) => {
    if (airport) {
      setEditingAirport(airport);
      setFormData({
        ma_san_bay: airport.ma_san_bay || "",
        ten_san_bay: airport.ten_san_bay || "",
        thanh_pho: airport.thanh_pho || "",
        quoc_gia: airport.quoc_gia || "",
      });
    } else {
      setEditingAirport(null);
      setFormData({
        ma_san_bay: "",
        ten_san_bay: "",
        thanh_pho: "",
        quoc_gia: "",
      });
    }
    setFormErrors({});
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingAirport(null);
    setFormData({
      ma_san_bay: "",
      ten_san_bay: "",
      thanh_pho: "",
      quoc_gia: "",
    });
    setFormErrors({});
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.ma_san_bay.trim()) {
      errors.ma_san_bay = "Mã sân bay là bắt buộc";
    } else if (formData.ma_san_bay.length > 10) {
      errors.ma_san_bay = "Mã sân bay không được quá 10 ký tự";
    }
    if (!formData.ten_san_bay.trim()) {
      errors.ten_san_bay = "Tên sân bay là bắt buộc";
    }
    if (!formData.thanh_pho.trim()) {
      errors.thanh_pho = "Thành phố là bắt buộc";
    }
    if (!formData.quoc_gia.trim()) {
      errors.quoc_gia = "Quốc gia là bắt buộc";
    }
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      setSubmitting(true);
      if (editingAirport) {
        await updateAirport(editingAirport.id, formData);
      } else {
        await createAirport(formData);
      }
      await loadAirports();
      handleCloseModal();
    } catch (err) {
      if (err.response?.data?.errors) {
        setFormErrors(err.response.data.errors);
      } else {
        alert(err.response?.data?.message || "Có lỗi xảy ra");
      }
      console.error("Error saving airport:", err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa sân bay này?")) {
      return;
    }

    try {
      await deleteAirport(id);
      await loadAirports();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể xóa sân bay");
      console.error("Error deleting airport:", err);
    }
  };

  const filteredAirports = airports.filter((airport) => {
    const search = searchTerm.toLowerCase();
    return (
      airport.ma_san_bay?.toLowerCase().includes(search) ||
      airport.ten_san_bay?.toLowerCase().includes(search) ||
      airport.thanh_pho?.toLowerCase().includes(search) ||
      airport.quoc_gia?.toLowerCase().includes(search)
    );
  });

  // Sort airports
  const sortedAirports = [...filteredAirports].sort((a, b) => {
    if (!sortField) return 0;

    const aValue = a[sortField]?.toString().toLowerCase() || "";
    const bValue = b[sortField]?.toString().toLowerCase() || "";

    if (sortDirection === "asc") {
      return aValue.localeCompare(bValue);
    } else {
      return bValue.localeCompare(aValue);
    }
  });

  // Pagination
  const totalPages = Math.ceil(sortedAirports.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedAirports = sortedAirports.slice(startIndex, endIndex);

  // Reset to page 1 when search or sort changes
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, sortField, sortDirection]);

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

  return (
    <DashboardLayout menuItems={adminMenuItems} title="Quản lý Sân Bay">
      <div className="airport-management-page">
        <div className="page-header">
          <div className="header-content">
            {/* <h2>Quản lý Sân Bay</h2> */}
            <p>Thêm, sửa, xóa thông tin sân bay trong hệ thống</p>
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
                placeholder="Tìm kiếm sân bay..."
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
              Thêm sân bay
            </button>
          </div>
        </div>

        {loading && airports.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách sân bay...</p>
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
            <button className="btn-retry" onClick={loadAirports}>
              Thử lại
            </button>
          </div>
        ) : filteredAirports.length === 0 ? (
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
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                <circle cx="12" cy="10" r="3" />
              </svg>
            </div>
            <h3>
              {searchTerm ? "Không tìm thấy sân bay" : "Không có sân bay nào"}
            </h3>
            <p>
              {searchTerm
                ? "Thử tìm kiếm với từ khóa khác"
                : "Bắt đầu bằng cách thêm sân bay mới"}
            </p>
            {!searchTerm && (
              <button className="btn-primary" onClick={() => handleOpenModal()}>
                Thêm sân bay đầu tiên
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="airports-table-container">
              <table className="airports-table">
                <thead>
                  <tr>
                    <th
                      className="sortable"
                      onClick={() => handleSort("ma_san_bay")}
                    >
                      <div className="th-content">
                        Mã sân bay
                        {getSortIcon("ma_san_bay")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("ten_san_bay")}
                    >
                      <div className="th-content">
                        Tên sân bay
                        {getSortIcon("ten_san_bay")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("thanh_pho")}
                    >
                      <div className="th-content">
                        Thành phố
                        {getSortIcon("thanh_pho")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("quoc_gia")}
                    >
                      <div className="th-content">
                        Quốc gia
                        {getSortIcon("quoc_gia")}
                      </div>
                    </th>
                    <th className="th-actions">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedAirports.map((airport) => (
                    <tr key={airport.id}>
                      <td>
                        <span className="airport-code">
                          {airport.ma_san_bay}
                        </span>
                      </td>
                      <td>{airport.ten_san_bay}</td>
                      <td>{airport.thanh_pho}</td>
                      <td>{airport.quoc_gia}</td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-edit"
                            onClick={() => handleOpenModal(airport)}
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
                            onClick={() => handleDelete(airport.id)}
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
                  {Math.min(endIndex, sortedAirports.length)} trong tổng số{" "}
                  {sortedAirports.length} sân bay
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
        {showModal && (
          <div className="modal-overlay" onClick={handleCloseModal}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h3>{editingAirport ? "Sửa sân bay" : "Thêm sân bay mới"}</h3>
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
                  <label htmlFor="ma_san_bay">
                    Mã sân bay <span className="required">*</span>
                  </label>
                  <input
                    id="ma_san_bay"
                    type="text"
                    value={formData.ma_san_bay}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        ma_san_bay: e.target.value.toUpperCase(),
                      })
                    }
                    className={formErrors.ma_san_bay ? "error" : ""}
                    placeholder="VD: SGN"
                    maxLength={10}
                    disabled={submitting}
                  />
                  {formErrors.ma_san_bay && (
                    <span className="error-message">
                      {formErrors.ma_san_bay}
                    </span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="ten_san_bay">
                    Tên sân bay <span className="required">*</span>
                  </label>
                  <input
                    id="ten_san_bay"
                    type="text"
                    value={formData.ten_san_bay}
                    onChange={(e) =>
                      setFormData({ ...formData, ten_san_bay: e.target.value })
                    }
                    className={formErrors.ten_san_bay ? "error" : ""}
                    placeholder="VD: Tan Son Nhat International Airport"
                    disabled={submitting}
                  />
                  {formErrors.ten_san_bay && (
                    <span className="error-message">
                      {formErrors.ten_san_bay}
                    </span>
                  )}
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="thanh_pho">
                      Thành phố <span className="required">*</span>
                    </label>
                    <input
                      id="thanh_pho"
                      type="text"
                      value={formData.thanh_pho}
                      onChange={(e) =>
                        setFormData({ ...formData, thanh_pho: e.target.value })
                      }
                      className={formErrors.thanh_pho ? "error" : ""}
                      placeholder="VD: Ho Chi Minh"
                      disabled={submitting}
                    />
                    {formErrors.thanh_pho && (
                      <span className="error-message">
                        {formErrors.thanh_pho}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="quoc_gia">
                      Quốc gia <span className="required">*</span>
                    </label>
                    <input
                      id="quoc_gia"
                      type="text"
                      value={formData.quoc_gia}
                      onChange={(e) =>
                        setFormData({ ...formData, quoc_gia: e.target.value })
                      }
                      className={formErrors.quoc_gia ? "error" : ""}
                      placeholder="VD: Vietnam"
                      disabled={submitting}
                    />
                    {formErrors.quoc_gia && (
                      <span className="error-message">
                        {formErrors.quoc_gia}
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
                    ) : editingAirport ? (
                      "Cập nhật"
                    ) : (
                      "Tạo mới"
                    )}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
