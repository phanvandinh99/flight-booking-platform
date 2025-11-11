import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getFlightList, getAirlines } from "../../api/customer";
import { useAuth } from "../../auth/AuthContext";
import "../../styles/flightList.css";

export default function FlightList() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [flights, setFlights] = useState([]);
  const [airlines, setAirlines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({
    current_page: 1,
    last_page: 1,
    per_page: 12,
    total: 0,
  });

  const [filters, setFilters] = useState({
    hang_hang_khong: "",
    loai_may_bay: "",
    gia_tu: "",
    gia_den: "",
    sort_by: "gio_khoi_hanh",
    sort_order: "asc",
  });

  useEffect(() => {
    loadAirlines();
    loadFlights();
  }, []);

  useEffect(() => {
    loadFlights();
  }, [filters, pagination.current_page]);

  const loadAirlines = async () => {
    try {
      const response = await getAirlines();
      setAirlines(response.data || []);
    } catch (err) {
      console.error("Error loading airlines:", err);
    }
  };

  const loadFlights = async () => {
    try {
      setLoading(true);
      const params = {
        page: pagination.current_page,
        per_page: pagination.per_page,
        ...filters,
      };

      // Remove empty filters
      Object.keys(params).forEach((key) => {
        if (
          params[key] === "" ||
          params[key] === null ||
          params[key] === undefined
        ) {
          delete params[key];
        }
      });

      // Convert price filters to numbers if they exist
      if (params.gia_tu) params.gia_tu = parseFloat(params.gia_tu);
      if (params.gia_den) params.gia_den = parseFloat(params.gia_den);

      const response = await getFlightList(params);
      setFlights(response.data || []);
      setPagination(response.pagination || pagination);
    } catch (err) {
      console.error("Error loading flights:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (field, value) => {
    setFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
    setPagination((prev) => ({
      ...prev,
      current_page: 1,
    }));
  };

  const handlePageChange = (page) => {
    setPagination((prev) => ({
      ...prev,
      current_page: page,
    }));
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount || 0);
  };

  const formatTime = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
    });
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
    <div className="flight-list-page">
      {/* Header - Kế thừa từ CustomerHome */}
      <header className="customer-header">
        <div className="header-content">
          <div className="logo" onClick={() => navigate("/")}>
            <svg
              width="32"
              height="32"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" />
            </svg>
            <span>Flight Booking</span>
          </div>
          <nav className="header-nav">
            <a href="/" className="nav-link">
              Trang chủ
            </a>
            <a href="/flights" className="nav-link active">
              Danh sách chuyến bay
            </a>
            <a href="#about" className="nav-link">
              Về chúng tôi
            </a>
            <a href="#help" className="nav-link">
              Trợ giúp
            </a>
            <a href="#guide" className="nav-link">
              Hướng dẫn đặt vé
            </a>
            {user ? (
              <>
                <a href="/bookings" className="nav-link">
                  Đặt vé của tôi
                </a>
                <span className="user-info">{user.ten || user.email}</span>
                <a
                  href="/login"
                  className="nav-link"
                  onClick={(e) => {
                    e.preventDefault();
                    localStorage.removeItem("fb_token");
                    localStorage.removeItem("fb_user");
                    window.location.href = "/";
                  }}
                >
                  Đăng xuất
                </a>
              </>
            ) : (
              <a href="/login" className="nav-link">
                Đăng nhập
              </a>
            )}
          </nav>
        </div>
      </header>

      <main className="flight-list-main">
        <div className="section-container">
          <h1 className="page-title">Danh sách chuyến bay</h1>

          {/* Filters */}
          <div className="filters-section">
            <div className="filter-group">
              <label>Hãng hàng không</label>
              <select
                value={filters.hang_hang_khong}
                onChange={(e) =>
                  handleFilterChange("hang_hang_khong", e.target.value)
                }
              >
                <option value="">Tất cả</option>
                {airlines.map((airline) => (
                  <option key={airline.id} value={airline.id}>
                    {airline.ten_hang}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label>Loại máy bay</label>
              <input
                type="text"
                placeholder="Ví dụ: Boeing 737, Airbus A320..."
                value={filters.loai_may_bay}
                onChange={(e) =>
                  handleFilterChange("loai_may_bay", e.target.value)
                }
              />
            </div>

            <div className="filter-group price-filter">
              <label>Giá từ (VNĐ)</label>
              <input
                type="number"
                placeholder="0"
                min="0"
                value={filters.gia_tu}
                onChange={(e) => handleFilterChange("gia_tu", e.target.value)}
              />
            </div>

            <div className="filter-group price-filter">
              <label>Giá đến (VNĐ)</label>
              <input
                type="number"
                placeholder="Không giới hạn"
                min="0"
                value={filters.gia_den}
                onChange={(e) => handleFilterChange("gia_den", e.target.value)}
              />
            </div>

            <div className="filter-group">
              <label>Sắp xếp theo</label>
              <select
                value={filters.sort_by}
                onChange={(e) => handleFilterChange("sort_by", e.target.value)}
              >
                <option value="gio_khoi_hanh">Giờ cất cánh</option>
                <option value="gia">Giá tiền</option>
                <option value="loai_may_bay">Loại máy bay</option>
              </select>
            </div>

            <div className="filter-group">
              <label>Thứ tự</label>
              <select
                value={filters.sort_order}
                onChange={(e) =>
                  handleFilterChange("sort_order", e.target.value)
                }
              >
                <option value="asc">Tăng dần</option>
                <option value="desc">Giảm dần</option>
              </select>
            </div>
          </div>

          {/* Flights List */}
          {loading ? (
            <div className="loading-container">
              <div className="loading-spinner"></div>
              <p>Đang tải danh sách chuyến bay...</p>
            </div>
          ) : flights.length === 0 ? (
            <div className="no-flights">
              <p>Không tìm thấy chuyến bay nào</p>
            </div>
          ) : (
            <>
              <div className="flights-list">
                {flights.map((flight) => (
                  <div key={flight.id} className="flight-card">
                    <div className="flight-card-header">
                      <div className="airline-badge">
                        <span className="airline-name">
                          {flight.hang_hang_khong?.ten_hang}
                        </span>
                        <span className="flight-code-badge">
                          {flight.ma_chuyen_bay}
                        </span>
                      </div>
                      <div className="price-badge">
                        {flight.gia_ve && flight.gia_ve.length > 0 ? (
                          <>
                            <span className="price-label">Từ</span>
                            <span className="price-amount">
                              {formatCurrency(flight.gia_ve[0].gia)}
                            </span>
                          </>
                        ) : (
                          <span className="price-contact">Liên hệ</span>
                        )}
                      </div>
                    </div>

                    <div className="flight-route-section">
                      <div className="route-point">
                        <div className="route-dot departure"></div>
                        <div className="route-info">
                          <div className="airport-name-large">
                            {flight.tuyen_bay?.san_bay_di?.ten_san_bay ||
                              flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                          </div>
                          <div className="time-large">
                            {formatTime(flight.gio_khoi_hanh)}
                          </div>
                          <div className="date-small">
                            {formatDateTime(flight.gio_khoi_hanh).split(",")[0]}
                          </div>
                        </div>
                      </div>

                      <div className="route-connector">
                        <div className="flight-duration">
                          <svg
                            width="20"
                            height="20"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                          >
                            <circle cx="12" cy="12" r="10"></circle>
                            <polyline points="12 6 12 12 16 14"></polyline>
                          </svg>
                        </div>
                        <div className="route-line"></div>
                      </div>

                      <div className="route-point">
                        <div className="route-dot arrival"></div>
                        <div className="route-info">
                          <div className="airport-name-large">
                            {flight.tuyen_bay?.san_bay_den?.ten_san_bay ||
                              flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                          </div>
                          <div className="time-large">
                            {formatTime(flight.gio_ha_canh)}
                          </div>
                          <div className="date-small">
                            {formatDateTime(flight.gio_ha_canh).split(",")[0]}
                          </div>
                        </div>
                      </div>
                    </div>

                    <div className="flight-meta">
                      <div className="meta-item">
                        <svg
                          width="18"
                          height="18"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                        >
                          <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"></path>
                        </svg>
                        <span>{flight.may_bay?.loai_may_bay || "N/A"}</span>
                      </div>
                    </div>

                    <div className="flight-actions">
                      <button
                        className="btn-view-details"
                        onClick={() => navigate(`/search?flight=${flight.id}`)}
                      >
                        <svg
                          width="20"
                          height="20"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                        >
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                          <circle cx="12" cy="12" r="3" />
                        </svg>
                        Xem chi tiết
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              {/* Pagination */}
              {pagination.last_page > 1 && (
                <div className="pagination">
                  <button
                    className="pagination-btn"
                    disabled={pagination.current_page === 1}
                    onClick={() =>
                      handlePageChange(pagination.current_page - 1)
                    }
                  >
                    Trước
                  </button>
                  {Array.from({ length: pagination.last_page }, (_, i) => i + 1)
                    .filter(
                      (page) =>
                        page === 1 ||
                        page === pagination.last_page ||
                        (page >= pagination.current_page - 1 &&
                          page <= pagination.current_page + 1)
                    )
                    .map((page, index, array) => (
                      <React.Fragment key={page}>
                        {index > 0 && array[index - 1] !== page - 1 && (
                          <span className="pagination-ellipsis">...</span>
                        )}
                        <button
                          className={`pagination-btn ${
                            page === pagination.current_page ? "active" : ""
                          }`}
                          onClick={() => handlePageChange(page)}
                        >
                          {page}
                        </button>
                      </React.Fragment>
                    ))}
                  <button
                    className="pagination-btn"
                    disabled={pagination.current_page === pagination.last_page}
                    onClick={() =>
                      handlePageChange(pagination.current_page + 1)
                    }
                  >
                    Sau
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>

      {/* Footer - Kế thừa từ CustomerHome */}
      <footer className="customer-footer">
        <div className="footer-content">
          <div className="footer-section">
            <h3>Về chúng tôi</h3>
            <p>
              Hệ thống đặt vé máy bay trực tuyến hàng đầu, mang đến cho bạn trải
              nghiệm đặt vé nhanh chóng và tiện lợi.
            </p>
          </div>
          <div className="footer-section">
            <h3>Trợ giúp</h3>
            <ul>
              <li>
                <a href="#faq">Câu hỏi thường gặp</a>
              </li>
              <li>
                <a href="#contact">Liên hệ</a>
              </li>
              <li>
                <a href="#support">Hỗ trợ khách hàng</a>
              </li>
            </ul>
          </div>
          <div className="footer-section">
            <h3>Hướng dẫn</h3>
            <ul>
              <li>
                <a href="#guide-booking">Hướng dẫn đặt vé</a>
              </li>
              <li>
                <a href="#guide-payment">Hướng dẫn thanh toán</a>
              </li>
              <li>
                <a href="#guide-checkin">Hướng dẫn check-in</a>
              </li>
            </ul>
          </div>
          <div className="footer-section">
            <h3>Theo dõi chúng tôi</h3>
            <div className="social-links">
              <a href="#facebook" aria-label="Facebook">
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M24 12.073c0-6.627-5.373-12-12-12S0 5.446 0 12.073c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
                </svg>
              </a>
              <a href="#twitter" aria-label="Twitter">
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z" />
                </svg>
              </a>
              <a href="#instagram" aria-label="Instagram">
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z" />
                </svg>
              </a>
            </div>
          </div>
        </div>
        <div className="footer-bottom">
          <p>&copy; 2024 Flight Booking. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
