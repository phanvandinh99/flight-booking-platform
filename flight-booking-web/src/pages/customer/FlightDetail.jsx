import React, { useState, useEffect } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { getFlightDetail } from "../../api/customer";
import { useAuth } from "../../auth/AuthContext";
import "../../styles/flightDetail.css";

export default function FlightDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const [flight, setFlight] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedFareClass, setSelectedFareClass] = useState(null);

  // Support both route param and query param for backward compatibility
  const flightId = id || searchParams.get("flight");

  useEffect(() => {
    if (flightId) {
      loadFlightDetail();
    } else {
      setError("Không tìm thấy chuyến bay");
      setLoading(false);
    }
  }, [flightId]);

  const loadFlightDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getFlightDetail(flightId);
      setFlight(response.data);
      // Select first available fare class
      if (response.data.gia_ve && response.data.gia_ve.length > 0) {
        setSelectedFareClass(response.data.gia_ve[0]);
      }
    } catch (err) {
      setError(
        err.response?.data?.message || "Không thể tải chi tiết chuyến bay"
      );
      console.error("Error loading flight detail:", err);
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

  const formatTime = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
      weekday: "long",
      day: "2-digit",
      month: "long",
      year: "numeric",
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

  const calculateDuration = (departure, arrival) => {
    if (!departure || !arrival) return "N/A";
    const dep = new Date(departure);
    const arr = new Date(arrival);
    const diff = arr - dep;
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    return `${hours}h ${minutes}m`;
  };

  const getFareClassLabel = (hangVe) => {
    const labels = {
      pho_thong: "Phổ thông",
      pho_thong_cao_cap: "Phổ thông cao cấp",
      thuong_gia: "Thương gia",
      hang_nhat: "Hạng nhất",
    };
    return labels[hangVe] || hangVe;
  };

  const handleBookFlight = () => {
    if (!selectedFareClass) {
      alert("Vui lòng chọn hạng vé");
      return;
    }
    if (!user) {
      navigate("/login", { state: { from: `/flight/${flight.id}` } });
      return;
    }
    navigate("/booking", {
      state: {
        flight: flight,
        fareClass: selectedFareClass,
      },
    });
  };

  const renderHeader = () => (
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
          <a href="/flights" className="nav-link">
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
  );

  const renderFooter = () => (
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
  );

  if (loading) {
    return (
      <div className="flight-detail-page">
        {renderHeader()}
        <main className="detail-main">
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải chi tiết chuyến bay...</p>
          </div>
        </main>
        {renderFooter()}
      </div>
    );
  }

  if (error || !flight) {
    return (
      <div className="flight-detail-page">
        {renderHeader()}
        <main className="detail-main">
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
            <h3>Không tìm thấy chuyến bay</h3>
            <p>{error || "Chuyến bay không tồn tại"}</p>
            <button className="btn-retry" onClick={() => navigate("/")}>
              Về trang chủ
            </button>
          </div>
        </main>
        {renderFooter()}
      </div>
    );
  }

  return (
    <div className="flight-detail-page">
      {renderHeader()}

      <main className="detail-main">
        <div className="section-container">
          {/* Flight Header */}
          <div className="flight-header-card">
            <div className="flight-header-top">
              <div className="airline-section">
                <div className="airline-logo">
                  <svg
                    width="40"
                    height="40"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" />
                  </svg>
                </div>
                <div className="airline-info">
                  <h2>{flight.hang_hang_khong?.ten_hang || "N/A"}</h2>
                  <span className="flight-code-badge">
                    {flight.ma_chuyen_bay}
                  </span>
                </div>
              </div>
              <div className="flight-date">
                <span className="date-label">Ngày bay</span>
                <span className="date-value">
                  {formatDate(flight.gio_khoi_hanh)}
                </span>
              </div>
            </div>

            {/* Route Section */}
            <div className="flight-route-section">
              <div className="route-point departure">
                <div className="route-dot"></div>
                <div className="route-details">
                  <div className="airport-name-large">
                    {flight.tuyen_bay?.san_bay_di?.ten_san_bay ||
                      flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                  </div>
                  <div className="airport-location">
                    {flight.tuyen_bay?.san_bay_di?.thanh_pho || ""}
                  </div>
                  <div className="time-large">
                    {formatTime(flight.gio_khoi_hanh)}
                  </div>
                  <div className="airport-code-small">
                    {flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                  </div>
                </div>
              </div>

              <div className="route-connector">
                <div className="duration-badge">
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
                  <span>
                    {calculateDuration(
                      flight.gio_khoi_hanh,
                      flight.gio_ha_canh
                    )}
                  </span>
                </div>
                <div className="route-line"></div>
              </div>

              <div className="route-point arrival">
                <div className="route-dot"></div>
                <div className="route-details">
                  <div className="airport-name-large">
                    {flight.tuyen_bay?.san_bay_den?.ten_san_bay ||
                      flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                  </div>
                  <div className="airport-location">
                    {flight.tuyen_bay?.san_bay_den?.thanh_pho || ""}
                  </div>
                  <div className="time-large">
                    {formatTime(flight.gio_ha_canh)}
                  </div>
                  <div className="airport-code-small">
                    {flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Flight Info & Pricing */}
          <div className="content-grid">
            {/* Flight Information */}
            <div className="info-section">
              <h3 className="section-title">Thông tin chuyến bay</h3>

              <div className="info-card">
                <div className="info-item">
                  <span className="info-label">
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
                    Loại máy bay
                  </span>
                  <span className="info-value">
                    {flight.may_bay?.loai_may_bay || "N/A"}
                  </span>
                </div>

                <div className="info-item">
                  <span className="info-label">
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <circle cx="12" cy="12" r="10"></circle>
                      <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                    Thời gian bay
                  </span>
                  <span className="info-value">
                    {calculateDuration(
                      flight.gio_khoi_hanh,
                      flight.gio_ha_canh
                    )}
                  </span>
                </div>

                <div className="info-item">
                  <span className="info-label">
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                      <polyline points="14 2 14 8 20 8"></polyline>
                      <line x1="16" y1="13" x2="8" y2="13"></line>
                      <line x1="16" y1="17" x2="8" y2="17"></line>
                      <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                    Giờ khởi hành
                  </span>
                  <span className="info-value">
                    {formatDateTime(flight.gio_khoi_hanh)}
                  </span>
                </div>

                <div className="info-item">
                  <span className="info-label">
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                      <polyline points="14 2 14 8 20 8"></polyline>
                      <line x1="16" y1="13" x2="8" y2="13"></line>
                      <line x1="16" y1="17" x2="8" y2="17"></line>
                      <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                    Giờ hạ cánh
                  </span>
                  <span className="info-value">
                    {formatDateTime(flight.gio_ha_canh)}
                  </span>
                </div>

                <div className="info-item">
                  <span className="info-label">
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                    </svg>
                    Trạng thái
                  </span>
                  <span className="info-value">
                    <span className="status-badge du-kien">Dự kiến</span>
                  </span>
                </div>
              </div>
            </div>

            {/* Pricing Section */}
            <div className="pricing-section">
              <h3 className="section-title">Chọn hạng vé</h3>

              {flight.gia_ve && flight.gia_ve.length > 0 ? (
                <div className="fare-classes">
                  {flight.gia_ve.map((fare) => (
                    <div
                      key={fare.id}
                      className={`fare-class-card ${
                        selectedFareClass?.id === fare.id ? "selected" : ""
                      }`}
                      onClick={() => setSelectedFareClass(fare)}
                    >
                      <div className="fare-class-header">
                        <div className="fare-class-name">
                          {getFareClassLabel(fare.hang_ve)}
                        </div>
                        <div className="fare-class-price">
                          {formatCurrency(fare.gia)}
                        </div>
                      </div>
                      <div className="fare-class-details">
                        <div className="fare-detail-item">
                          <svg
                            width="16"
                            height="16"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                          >
                            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"></path>
                          </svg>
                          <span>
                            Hành lý ký gửi: {fare.hanh_ly_ky_gui || "20kg"}
                          </span>
                        </div>
                        {fare.chinh_sach_huy_ve && (
                          <div className="fare-detail-item">
                            <svg
                              width="16"
                              height="16"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2"
                            >
                              <circle cx="12" cy="12" r="10"></circle>
                              <line x1="12" y1="8" x2="12" y2="12"></line>
                              <line x1="12" y1="16" x2="12.01" y2="16"></line>
                            </svg>
                            <span>{fare.chinh_sach_huy_ve}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="no-fare-classes">
                  <p>Chưa có thông tin giá vé</p>
                </div>
              )}

              <div className="booking-section">
                <div className="selected-fare-summary">
                  {selectedFareClass ? (
                    <>
                      <div className="summary-item">
                        <span>Hạng vé:</span>
                        <span className="summary-value">
                          {getFareClassLabel(selectedFareClass.hang_ve)}
                        </span>
                      </div>
                      <div className="summary-item">
                        <span>Giá vé:</span>
                        <span className="summary-price">
                          {formatCurrency(selectedFareClass.gia)}
                        </span>
                      </div>
                    </>
                  ) : (
                    <p className="no-selection">Vui lòng chọn hạng vé</p>
                  )}
                </div>
                <button
                  className="btn-book-flight"
                  onClick={handleBookFlight}
                  disabled={!selectedFareClass}
                >
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                    <circle cx="8.5" cy="7" r="4"></circle>
                    <line x1="20" y1="8" x2="20" y2="14"></line>
                    <line x1="23" y1="11" x2="17" y2="11"></line>
                  </svg>
                  Đặt vé ngay
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      {renderFooter()}
    </div>
  );
}
