import React, { useState, useEffect } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { adminMenuItems } from "../../config/adminMenu";
import {
  getConfigs,
  createConfig,
  updateConfig,
  deleteConfig,
} from "../../api/admin";
import "../../styles/systemConfig.css";

export default function SystemConfig() {
  const [configs, setConfigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingConfig, setEditingConfig] = useState(null);
  const [formData, setFormData] = useState({
    ten_cau_hinh: "",
    gia_tri: "",
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDirection, setSortDirection] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);

  useEffect(() => {
    loadConfigs();
  }, []);

  const loadConfigs = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getConfigs();
      setConfigs(response.data || []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Không thể tải danh sách cấu hình"
      );
      console.error("Error loading configs:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (config = null) => {
    if (config) {
      setEditingConfig(config);
      setFormData({
        ten_cau_hinh: config.ten_cau_hinh || "",
        gia_tri: config.gia_tri || "",
      });
    } else {
      setEditingConfig(null);
      setFormData({
        ten_cau_hinh: "",
        gia_tri: "",
      });
    }
    setFormErrors({});
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingConfig(null);
    setFormData({
      ten_cau_hinh: "",
      gia_tri: "",
    });
    setFormErrors({});
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.ten_cau_hinh.trim()) {
      errors.ten_cau_hinh = "Tên cấu hình là bắt buộc";
    } else if (formData.ten_cau_hinh.length > 255) {
      errors.ten_cau_hinh = "Tên cấu hình không được quá 255 ký tự";
    }
    if (!formData.gia_tri.trim()) {
      errors.gia_tri = "Giá trị là bắt buộc";
    }
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      setSubmitting(true);
      if (editingConfig) {
        await updateConfig(editingConfig.ten_cau_hinh, {
          gia_tri: formData.gia_tri,
        });
      } else {
        await createConfig(formData);
      }
      await loadConfigs();
      handleCloseModal();
    } catch (err) {
      if (err.response?.data?.errors) {
        setFormErrors(err.response.data.errors);
      } else {
        alert(err.response?.data?.message || "Có lỗi xảy ra");
      }
      console.error("Error saving config:", err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (key) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa cấu hình này?")) {
      return;
    }

    try {
      await deleteConfig(key);
      await loadConfigs();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể xóa cấu hình");
      console.error("Error deleting config:", err);
    }
  };

  const filteredConfigs = configs.filter((config) => {
    const search = searchTerm.toLowerCase();
    return (
      config.ten_cau_hinh?.toLowerCase().includes(search) ||
      config.gia_tri?.toLowerCase().includes(search)
    );
  });

  // Sort configs
  const sortedConfigs = [...filteredConfigs].sort((a, b) => {
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
  const totalPages = Math.ceil(sortedConfigs.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedConfigs = sortedConfigs.slice(startIndex, endIndex);

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
    <DashboardLayout menuItems={adminMenuItems} title="Cấu Hình Hệ Thống">
      <div className="system-config-page">
        <div className="page-header">
          <div className="header-content">
            {/* <h2>Cấu Hình Hệ Thống</h2> */}
            <p>Quản lý các cấu hình và tham số hệ thống</p>
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
                placeholder="Tìm kiếm cấu hình..."
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
              Thêm cấu hình
            </button>
          </div>
        </div>

        {loading && configs.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách cấu hình...</p>
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
            <button className="btn-retry" onClick={loadConfigs}>
              Thử lại
            </button>
          </div>
        ) : filteredConfigs.length === 0 ? (
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
                <circle cx="12" cy="12" r="3" />
                <path d="M12 1v6m0 6v6M5.64 5.64l4.24 4.24m4.24 4.24l4.24 4.24M1 12h6m6 0h6M5.64 18.36l4.24-4.24m4.24-4.24l4.24-4.24" />
              </svg>
            </div>
            <h3>
              {searchTerm ? "Không tìm thấy cấu hình" : "Không có cấu hình nào"}
            </h3>
            <p>
              {searchTerm
                ? "Thử tìm kiếm với từ khóa khác"
                : "Bắt đầu bằng cách thêm cấu hình mới"}
            </p>
            {!searchTerm && (
              <button className="btn-primary" onClick={() => handleOpenModal()}>
                Thêm cấu hình đầu tiên
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="configs-table-container">
              <table className="configs-table">
                <thead>
                  <tr>
                    <th
                      className="sortable"
                      onClick={() => handleSort("ten_cau_hinh")}
                    >
                      <div className="th-content">
                        Tên Cấu Hình
                        {getSortIcon("ten_cau_hinh")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("gia_tri")}
                    >
                      <div className="th-content">
                        Giá Trị
                        {getSortIcon("gia_tri")}
                      </div>
                    </th>
                    <th className="th-actions">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedConfigs.map((config) => (
                    <tr key={config.id || config.ten_cau_hinh}>
                      <td>
                        <span className="config-key">
                          {config.ten_cau_hinh}
                        </span>
                      </td>
                      <td>
                        <span className="config-value">{config.gia_tri}</span>
                      </td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-edit"
                            onClick={() => handleOpenModal(config)}
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
                            onClick={() => handleDelete(config.ten_cau_hinh)}
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
                  {Math.min(endIndex, sortedConfigs.length)} trong tổng số{" "}
                  {sortedConfigs.length} cấu hình
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
                <h3>{editingConfig ? "Sửa cấu hình" : "Thêm cấu hình mới"}</h3>
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
                  <label htmlFor="ten_cau_hinh">
                    Tên Cấu Hình <span className="required">*</span>
                  </label>
                  <input
                    id="ten_cau_hinh"
                    type="text"
                    value={formData.ten_cau_hinh}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        ten_cau_hinh: e.target.value,
                      })
                    }
                    className={formErrors.ten_cau_hinh ? "error" : ""}
                    placeholder="VD: thue, phi_dich_vu, ..."
                    maxLength={255}
                    disabled={submitting || editingConfig !== null}
                  />
                  {editingConfig && (
                    <span className="field-note">
                      Không thể thay đổi tên cấu hình khi chỉnh sửa
                    </span>
                  )}
                  {formErrors.ten_cau_hinh && (
                    <span className="error-message">
                      {formErrors.ten_cau_hinh}
                    </span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="gia_tri">
                    Giá Trị <span className="required">*</span>
                  </label>
                  <input
                    id="gia_tri"
                    type="text"
                    value={formData.gia_tri}
                    onChange={(e) =>
                      setFormData({ ...formData, gia_tri: e.target.value })
                    }
                    className={formErrors.gia_tri ? "error" : ""}
                    placeholder="VD: 0.1, 100000, true, ..."
                    disabled={submitting}
                  />
                  {formErrors.gia_tri && (
                    <span className="error-message">{formErrors.gia_tri}</span>
                  )}
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
                    ) : editingConfig ? (
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
