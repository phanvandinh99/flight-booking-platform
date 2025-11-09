import React, { useState, useEffect } from "react";
import { createPortal } from "react-dom";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { airlineMenuItems } from "../../config/airlineMenu";
import {
  getAircrafts,
  createAircraft,
  updateAircraft,
  deleteAircraft,
} from "../../api/airline";
import "../../styles/airplaneManagement.css";

export default function AirplaneManagement() {
  const [aircrafts, setAircrafts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingAircraft, setEditingAircraft] = useState(null);
  const [formData, setFormData] = useState({
    loai_may_bay: "",
    tong_so_ghe: "",
    so_do_ghe: {
      pho_thong: [],
      thuong_gia: [],
    },
  });
  const [seatInput, setSeatInput] = useState({
    pho_thong: "",
    thuong_gia: "",
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDirection, setSortDirection] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);

  useEffect(() => {
    loadAircrafts();
  }, []);

  const loadAircrafts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getAircrafts();
      setAircrafts(response.data || []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Không thể tải danh sách máy bay"
      );
      console.error("Error loading aircrafts:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (aircraft = null) => {
    if (aircraft) {
      setEditingAircraft(aircraft);
      setFormData({
        loai_may_bay: aircraft.loai_may_bay || "",
        tong_so_ghe: aircraft.tong_so_ghe || "",
        so_do_ghe: aircraft.so_do_ghe || {
          pho_thong: [],
          thuong_gia: [],
        },
      });
      setSeatInput({
        pho_thong: Array.isArray(aircraft.so_do_ghe?.pho_thong)
          ? aircraft.so_do_ghe.pho_thong.join(", ")
          : "",
        thuong_gia: Array.isArray(aircraft.so_do_ghe?.thuong_gia)
          ? aircraft.so_do_ghe.thuong_gia.join(", ")
          : "",
      });
    } else {
      setEditingAircraft(null);
      setFormData({
        loai_may_bay: "",
        tong_so_ghe: "",
        so_do_ghe: {
          pho_thong: [],
          thuong_gia: [],
        },
      });
      setSeatInput({
        pho_thong: "",
        thuong_gia: "",
      });
    }
    setFormErrors({});
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingAircraft(null);
    setFormData({
      loai_may_bay: "",
      tong_so_ghe: "",
      so_do_ghe: {
        pho_thong: [],
        thuong_gia: [],
      },
    });
    setSeatInput({
      pho_thong: "",
      thuong_gia: "",
    });
    setFormErrors({});
  };

  const handleSeatInputChange = (type, value) => {
    setSeatInput({ ...seatInput, [type]: value });
    const seats = value
      .split(",")
      .map((s) => s.trim())
      .filter((s) => s.length > 0);
    setFormData({
      ...formData,
      so_do_ghe: {
        ...formData.so_do_ghe,
        [type]: seats,
      },
    });
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.loai_may_bay.trim()) {
      errors.loai_may_bay = "Loại máy bay là bắt buộc";
    }
    if (!formData.tong_so_ghe) {
      errors.tong_so_ghe = "Tổng số ghế là bắt buộc";
    } else if (isNaN(formData.tong_so_ghe) || formData.tong_so_ghe < 1) {
      errors.tong_so_ghe = "Tổng số ghế phải là số nguyên dương";
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
        loai_may_bay: formData.loai_may_bay,
        tong_so_ghe: parseInt(formData.tong_so_ghe),
        so_do_ghe: formData.so_do_ghe,
      };
      if (editingAircraft) {
        await updateAircraft(editingAircraft.id, submitData);
      } else {
        await createAircraft(submitData);
      }
      await loadAircrafts();
      handleCloseModal();
    } catch (err) {
      if (err.response?.data?.errors) {
        setFormErrors(err.response.data.errors);
      } else {
        alert(err.response?.data?.message || "Có lỗi xảy ra");
      }
      console.error("Error saving aircraft:", err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa máy bay này?")) {
      return;
    }

    try {
      await deleteAircraft(id);
      await loadAircrafts();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể xóa máy bay");
      console.error("Error deleting aircraft:", err);
    }
  };

  const filteredAircrafts = aircrafts.filter((aircraft) => {
    const search = searchTerm.toLowerCase();
    return (
      aircraft.loai_may_bay?.toLowerCase().includes(search) ||
      aircraft.tong_so_ghe?.toString().includes(search) ||
      aircraft.hang_hang_khong?.ten_hang?.toLowerCase().includes(search)
    );
  });

  // Sort aircrafts
  const sortedAircrafts = [...filteredAircrafts].sort((a, b) => {
    if (!sortField) return 0;

    let aValue, bValue;
    if (sortField === "hang_hang_khong") {
      aValue = a.hang_hang_khong?.ten_hang?.toLowerCase() || "";
      bValue = b.hang_hang_khong?.ten_hang?.toLowerCase() || "";
    } else {
      aValue = a[sortField]?.toString().toLowerCase() || "";
      bValue = b[sortField]?.toString().toLowerCase() || "";
    }

    if (sortDirection === "asc") {
      return aValue.localeCompare(bValue);
    } else {
      return bValue.localeCompare(aValue);
    }
  });

  // Pagination
  const totalPages = Math.ceil(sortedAircrafts.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedAircrafts = sortedAircrafts.slice(startIndex, endIndex);

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
    <DashboardLayout menuItems={airlineMenuItems} title="Quản lý Máy Bay">
      <div className="airplane-management-page">
        <div className="page-header">
          <div className="header-content">
            <h2>Quản lý Máy Bay</h2>
            <p>Thêm, sửa, xóa thông tin máy bay trong hệ thống</p>
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
                placeholder="Tìm kiếm máy bay..."
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
              Thêm máy bay
            </button>
          </div>
        </div>

        {loading && aircrafts.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách máy bay...</p>
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
            <button className="btn-retry" onClick={loadAircrafts}>
              Thử lại
            </button>
          </div>
        ) : filteredAircrafts.length === 0 ? (
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
            <h3>
              {searchTerm ? "Không tìm thấy máy bay" : "Không có máy bay nào"}
            </h3>
            <p>
              {searchTerm
                ? "Thử tìm kiếm với từ khóa khác"
                : "Bắt đầu bằng cách thêm máy bay mới"}
            </p>
            {!searchTerm && (
              <button className="btn-primary" onClick={() => handleOpenModal()}>
                Thêm máy bay đầu tiên
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="aircrafts-table-container">
              <table className="aircrafts-table">
                <thead>
                  <tr>
                    <th
                      className="sortable"
                      onClick={() => handleSort("loai_may_bay")}
                    >
                      <div className="th-content">
                        Loại máy bay
                        {getSortIcon("loai_may_bay")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("tong_so_ghe")}
                    >
                      <div className="th-content">
                        Tổng số ghế
                        {getSortIcon("tong_so_ghe")}
                      </div>
                    </th>
                    <th>Sơ đồ ghế</th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("hang_hang_khong")}
                    >
                      <div className="th-content">
                        Hãng hàng không
                        {getSortIcon("hang_hang_khong")}
                      </div>
                    </th>
                    <th className="th-actions">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedAircrafts.map((aircraft) => (
                    <tr key={aircraft.id}>
                      <td>
                        <span className="aircraft-type">
                          {aircraft.loai_may_bay}
                        </span>
                      </td>
                      <td>{aircraft.tong_so_ghe}</td>
                      <td>
                        <div className="seat-info">
                          <span className="seat-badge">
                            Phổ thông:{" "}
                            {Array.isArray(aircraft.so_do_ghe?.pho_thong)
                              ? aircraft.so_do_ghe.pho_thong.length
                              : 0}
                          </span>
                          <span className="seat-badge">
                            Thương gia:{" "}
                            {Array.isArray(aircraft.so_do_ghe?.thuong_gia)
                              ? aircraft.so_do_ghe.thuong_gia.length
                              : 0}
                          </span>
                        </div>
                      </td>
                      <td>{aircraft.hang_hang_khong?.ten_hang || "N/A"}</td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-edit"
                            onClick={() => handleOpenModal(aircraft)}
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
                            onClick={() => handleDelete(aircraft.id)}
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
                  {Math.min(endIndex, sortedAircrafts.length)} trong tổng số{" "}
                  {sortedAircrafts.length} máy bay
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

        {/* Modal Form - Rendered using Portal */}
        {showModal &&
          createPortal(
            <div className="modal-overlay" onClick={handleCloseModal}>
              <div
                className="modal-content"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="modal-header">
                  <h3>
                    {editingAircraft ? "Sửa máy bay" : "Thêm máy bay mới"}
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
                  <div className="form-group">
                    <label htmlFor="loai_may_bay">
                      Loại máy bay <span className="required">*</span>
                    </label>
                    <input
                      id="loai_may_bay"
                      type="text"
                      value={formData.loai_may_bay}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          loai_may_bay: e.target.value,
                        })
                      }
                      className={formErrors.loai_may_bay ? "error" : ""}
                      placeholder="VD: Boeing 787-9, Airbus A321"
                      disabled={submitting}
                    />
                    {formErrors.loai_may_bay && (
                      <span className="error-message">
                        {formErrors.loai_may_bay}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="tong_so_ghe">
                      Tổng số ghế <span className="required">*</span>
                    </label>
                    <input
                      id="tong_so_ghe"
                      type="number"
                      min="1"
                      value={formData.tong_so_ghe}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          tong_so_ghe: e.target.value,
                        })
                      }
                      className={formErrors.tong_so_ghe ? "error" : ""}
                      placeholder="VD: 270"
                      disabled={submitting}
                    />
                    {formErrors.tong_so_ghe && (
                      <span className="error-message">
                        {formErrors.tong_so_ghe}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="so_do_ghe_pho_thong">
                      Sơ đồ ghế - Phổ thông
                    </label>
                    <input
                      id="so_do_ghe_pho_thong"
                      type="text"
                      value={seatInput.pho_thong}
                      onChange={(e) =>
                        handleSeatInputChange("pho_thong", e.target.value)
                      }
                      placeholder="VD: 1A, 1B, 1C, 2A, 2B (phân cách bằng dấu phẩy)"
                      disabled={submitting}
                    />
                    <small className="form-hint">
                      Nhập mã ghế phân cách bằng dấu phẩy
                    </small>
                  </div>

                  <div className="form-group">
                    <label htmlFor="so_do_ghe_thuong_gia">
                      Sơ đồ ghế - Thương gia
                    </label>
                    <input
                      id="so_do_ghe_thuong_gia"
                      type="text"
                      value={seatInput.thuong_gia}
                      onChange={(e) =>
                        handleSeatInputChange("thuong_gia", e.target.value)
                      }
                      placeholder="VD: 10A, 10B, 11A, 11B (phân cách bằng dấu phẩy)"
                      disabled={submitting}
                    />
                    <small className="form-hint">
                      Nhập mã ghế phân cách bằng dấu phẩy
                    </small>
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
                      ) : editingAircraft ? (
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
