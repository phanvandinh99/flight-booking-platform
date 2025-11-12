import React, { useState, useEffect } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { adminMenuItems } from "../../config/adminMenu";
import {
  getRoutes,
  createRoute,
  updateRoute,
  deleteRoute,
  approveRoute,
  revokeRoute,
  getAirports,
} from "../../api/admin";
import "../../styles/routeManagement.css";

export default function RouteManagement() {
  const [routes, setRoutes] = useState([]);
  const [airports, setAirports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingRoute, setEditingRoute] = useState(null);
  const [formData, setFormData] = useState({
    san_bay_di: "",
    san_bay_den: "",
    duoc_phe_duyet: false,
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDirection, setSortDirection] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [routesResponse, airportsResponse] = await Promise.all([
        getRoutes(),
        getAirports(),
      ]);
      setRoutes(routesResponse.data || []);
      setAirports(airportsResponse.data || []);
    } catch (err) {
      setError(
        err.response?.data?.message || "Không thể tải danh sách tuyến bay"
      );
      console.error("Error loading data:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (route = null) => {
    if (route) {
      setEditingRoute(route);
      setFormData({
        san_bay_di: route.san_bay_di?.id || route.san_bay_di || "",
        san_bay_den: route.san_bay_den?.id || route.san_bay_den || "",
        duoc_phe_duyet: route.duoc_phe_duyet || false,
      });
    } else {
      setEditingRoute(null);
      setFormData({
        san_bay_di: "",
        san_bay_den: "",
        duoc_phe_duyet: false,
      });
    }
    setFormErrors({});
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingRoute(null);
    setFormData({
      san_bay_di: "",
      san_bay_den: "",
      duoc_phe_duyet: false,
    });
    setFormErrors({});
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.san_bay_di) {
      errors.san_bay_di = "Sân bay đi là bắt buộc";
    }
    if (!formData.san_bay_den) {
      errors.san_bay_den = "Sân bay đến là bắt buộc";
    }
    if (formData.san_bay_di === formData.san_bay_den) {
      errors.san_bay_den = "Sân bay đến phải khác sân bay đi";
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
        san_bay_di: parseInt(formData.san_bay_di),
        san_bay_den: parseInt(formData.san_bay_den),
        duoc_phe_duyet: formData.duoc_phe_duyet,
      };
      if (editingRoute) {
        await updateRoute(editingRoute.id, submitData);
      } else {
        await createRoute(submitData);
      }
      await loadData();
      handleCloseModal();
    } catch (err) {
      if (err.response?.data?.errors) {
        setFormErrors(err.response.data.errors);
      } else {
        alert(err.response?.data?.message || "Có lỗi xảy ra");
      }
      console.error("Error saving route:", err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa tuyến bay này?")) {
      return;
    }

    try {
      await deleteRoute(id);
      await loadData();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể xóa tuyến bay");
      console.error("Error deleting route:", err);
    }
  };

  const handleApprove = async (id) => {
    try {
      await approveRoute(id);
      await loadData();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể phê duyệt tuyến bay");
      console.error("Error approving route:", err);
    }
  };

  const handleRevoke = async (id) => {
    if (
      !window.confirm("Bạn có chắc chắn muốn thu hồi phê duyệt tuyến bay này?")
    ) {
      return;
    }
    try {
      await revokeRoute(id);
      await loadData();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể thu hồi phê duyệt");
      console.error("Error revoking route:", err);
    }
  };

  const filteredRoutes = routes.filter((route) => {
    const search = searchTerm.toLowerCase();
    const sanBayDi =
      route.san_bay_di?.ten_san_bay || route.san_bay_di?.ma_san_bay || "";
    const sanBayDen =
      route.san_bay_den?.ten_san_bay || route.san_bay_den?.ma_san_bay || "";
    return (
      sanBayDi.toLowerCase().includes(search) ||
      sanBayDen.toLowerCase().includes(search) ||
      route.san_bay_di?.ma_san_bay?.toLowerCase().includes(search) ||
      route.san_bay_den?.ma_san_bay?.toLowerCase().includes(search)
    );
  });

  // Sort routes
  const sortedRoutes = [...filteredRoutes].sort((a, b) => {
    if (!sortField) return 0;

    let aValue, bValue;
    if (sortField === "san_bay_di") {
      aValue = a.san_bay_di?.ten_san_bay || a.san_bay_di?.ma_san_bay || "";
      bValue = b.san_bay_di?.ten_san_bay || b.san_bay_di?.ma_san_bay || "";
    } else if (sortField === "san_bay_den") {
      aValue = a.san_bay_den?.ten_san_bay || a.san_bay_den?.ma_san_bay || "";
      bValue = b.san_bay_den?.ten_san_bay || b.san_bay_den?.ma_san_bay || "";
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
  const totalPages = Math.ceil(sortedRoutes.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedRoutes = sortedRoutes.slice(startIndex, endIndex);

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
    <DashboardLayout menuItems={adminMenuItems} title="Quản lý Tuyến Bay">
      <div className="route-management-page">
        <div className="page-header">
          <div className="header-content">
            {/* <h2>Quản lý Tuyến Bay</h2> */}
            <p>Thêm, sửa, xóa và phê duyệt tuyến bay trong hệ thống</p>
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
                placeholder="Tìm kiếm tuyến bay..."
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
              Thêm tuyến bay
            </button>
          </div>
        </div>

        {loading && routes.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách tuyến bay...</p>
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
            <button className="btn-retry" onClick={loadData}>
              Thử lại
            </button>
          </div>
        ) : filteredRoutes.length === 0 ? (
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
              {searchTerm
                ? "Không tìm thấy tuyến bay"
                : "Không có tuyến bay nào"}
            </h3>
            <p>
              {searchTerm
                ? "Thử tìm kiếm với từ khóa khác"
                : "Bắt đầu bằng cách thêm tuyến bay mới"}
            </p>
            {!searchTerm && (
              <button className="btn-primary" onClick={() => handleOpenModal()}>
                Thêm tuyến bay đầu tiên
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="routes-table-container">
              <table className="routes-table">
                <thead>
                  <tr>
                    <th
                      className="sortable"
                      onClick={() => handleSort("san_bay_di")}
                    >
                      <div className="th-content">
                        Sân bay đi
                        {getSortIcon("san_bay_di")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("san_bay_den")}
                    >
                      <div className="th-content">
                        Sân bay đến
                        {getSortIcon("san_bay_den")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("duoc_phe_duyet")}
                    >
                      <div className="th-content">
                        Trạng thái
                        {getSortIcon("duoc_phe_duyet")}
                      </div>
                    </th>
                    <th className="th-actions">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedRoutes.map((route) => (
                    <tr key={route.id}>
                      <td>
                        <div className="airport-info">
                          <span className="airport-code">
                            {route.san_bay_di?.ma_san_bay || "N/A"}
                          </span>
                          <span className="airport-name">
                            {route.san_bay_di?.ten_san_bay || "N/A"}
                          </span>
                        </div>
                      </td>
                      <td>
                        <div className="airport-info">
                          <span className="airport-code">
                            {route.san_bay_den?.ma_san_bay || "N/A"}
                          </span>
                          <span className="airport-name">
                            {route.san_bay_den?.ten_san_bay || "N/A"}
                          </span>
                        </div>
                      </td>
                      <td>
                        {route.duoc_phe_duyet ? (
                          <span className="status-badge status-approved">
                            Đã phê duyệt
                          </span>
                        ) : (
                          <span className="status-badge status-pending">
                            Chờ phê duyệt
                          </span>
                        )}
                      </td>
                      <td>
                        <div className="action-buttons">
                          {!route.duoc_phe_duyet ? (
                            <button
                              className="btn-approve"
                              onClick={() => handleApprove(route.id)}
                              title="Phê duyệt"
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
                                <polyline points="20 6 9 17 4 12" />
                              </svg>
                            </button>
                          ) : (
                            <button
                              className="btn-revoke"
                              onClick={() => handleRevoke(route.id)}
                              title="Thu hồi phê duyệt"
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
                                <line x1="18" y1="6" x2="6" y2="18" />
                                <line x1="6" y1="6" x2="18" y2="18" />
                              </svg>
                            </button>
                          )}
                          <button
                            className="btn-edit"
                            onClick={() => handleOpenModal(route)}
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
                            onClick={() => handleDelete(route.id)}
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
                  {Math.min(endIndex, sortedRoutes.length)} trong tổng số{" "}
                  {sortedRoutes.length} tuyến bay
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
                <h3>{editingRoute ? "Sửa tuyến bay" : "Thêm tuyến bay mới"}</h3>
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
                    <label htmlFor="san_bay_di">
                      Sân bay đi <span className="required">*</span>
                    </label>
                    <select
                      id="san_bay_di"
                      value={formData.san_bay_di}
                      onChange={(e) =>
                        setFormData({ ...formData, san_bay_di: e.target.value })
                      }
                      className={formErrors.san_bay_di ? "error" : ""}
                      disabled={submitting}
                    >
                      <option value="">Chọn sân bay đi</option>
                      {airports.map((airport) => (
                        <option key={airport.id} value={airport.id}>
                          {airport.ma_san_bay} - {airport.ten_san_bay}
                        </option>
                      ))}
                    </select>
                    {formErrors.san_bay_di && (
                      <span className="error-message">
                        {formErrors.san_bay_di}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="san_bay_den">
                      Sân bay đến <span className="required">*</span>
                    </label>
                    <select
                      id="san_bay_den"
                      value={formData.san_bay_den}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          san_bay_den: e.target.value,
                        })
                      }
                      className={formErrors.san_bay_den ? "error" : ""}
                      disabled={submitting}
                    >
                      <option value="">Chọn sân bay đến</option>
                      {airports
                        .filter(
                          (airport) =>
                            airport.id !== parseInt(formData.san_bay_di)
                        )
                        .map((airport) => (
                          <option key={airport.id} value={airport.id}>
                            {airport.ma_san_bay} - {airport.ten_san_bay}
                          </option>
                        ))}
                    </select>
                    {formErrors.san_bay_den && (
                      <span className="error-message">
                        {formErrors.san_bay_den}
                      </span>
                    )}
                  </div>
                </div>

                <div className="form-group">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={formData.duoc_phe_duyet}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          duoc_phe_duyet: e.target.checked,
                        })
                      }
                      disabled={submitting}
                    />
                    <span>Được phê duyệt</span>
                  </label>
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
                    ) : editingRoute ? (
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
