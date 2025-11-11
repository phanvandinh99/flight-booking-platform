import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  getBookings,
  getBooking,
  createPaymentUrl,
  cancelBooking,
} from "../../api/customer";
import { useAuth } from "../../auth/AuthContext";
import "../../styles/myBookings.css";

export default function MyBookings() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [processingPayment, setProcessingPayment] = useState(null);
  const [cancelling, setCancelling] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  useEffect(() => {
    // Check authentication
    const token = localStorage.getItem("fb_token");
    if (!user || !token) {
      navigate("/login", {
        state: {
          from: "/bookings",
          message: "Vui lòng đăng nhập để xem đặt vé của bạn",
        },
      });
      return;
    }

    // Check user role
    if (user.vai_tro !== "khach_hang") {
      setError(
        "Bạn không có quyền xem đặt vé. Vui lòng đăng nhập bằng tài khoản khách hàng."
      );
      return;
    }

    loadBookings();
  }, [user, navigate]);

  const loadBookings = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getBookings();
      setBookings(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Không thể tải danh sách đặt vé");
      console.error("Error loading bookings:", err);
    } finally {
      setLoading(false);
    }
  };

  const handlePayment = async (bookingId) => {
    try {
      setProcessingPayment(bookingId);
      setError(null);

      const response = await createPaymentUrl(bookingId);
      // Redirect đến VNPAY
      window.location.href = response.data.payment_url;
    } catch (err) {
      setError(err.response?.data?.message || "Không thể tạo URL thanh toán");
      console.error("Error creating payment URL:", err);
      setProcessingPayment(null);
    }
  };

  const handleCancel = async (bookingId) => {
    if (!window.confirm("Bạn có chắc chắn muốn hủy đặt vé này không?")) {
      return;
    }

    try {
      setCancelling(bookingId);
      setError(null);

      await cancelBooking(bookingId);
      // Reload danh sách
      await loadBookings();
      alert("Hủy đặt vé thành công!");
    } catch (err) {
      setError(err.response?.data?.message || "Không thể hủy đặt vé");
      console.error("Error cancelling booking:", err);
    } finally {
      setCancelling(null);
    }
  };

  const handleViewDetail = async (bookingId) => {
    try {
      setLoadingDetail(true);
      setError(null);
      const response = await getBooking(bookingId);
      setSelectedBooking(response.data);
      setShowDetailModal(true);
    } catch (err) {
      setError(err.response?.data?.message || "Không thể tải chi tiết đặt vé");
      console.error("Error loading booking detail:", err);
    } finally {
      setLoadingDetail(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount || 0);
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("vi-VN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  };

  const getStatusLabel = (status) => {
    const statusMap = {
      cho_thanh_toan: "Chờ thanh toán",
      da_thanh_toan: "Đã thanh toán",
      da_huy: "Đã hủy",
      da_hoan_tien: "Đã hoàn tiền",
      het_han: "Hết hạn",
    };
    return statusMap[status] || status;
  };

  const getStatusClass = (status) => {
    const classMap = {
      cho_thanh_toan: "status-waiting",
      giu_cho: "status-waiting",
      da_thanh_toan: "status-paid",
      da_huy: "status-cancelled",
      da_hoan_tien: "status-refunded",
      het_han: "status-expired",
    };
    return classMap[status] || "status-unknown";
  };

  const getPassengerTypeLabel = (type) => {
    const typeMap = {
      nguoi_lon: "Người lớn",
      tre_em: "Trẻ em",
      em_be: "Em bé",
    };
    return typeMap[type] || type;
  };

  const getFareClassLabel = (hangVe) => {
    const labelMap = {
      hang_nhat: "Hạng nhất",
      thuong_gia: "Thương gia",
      pho_thong_cao_cap: "Phổ thông cao cấp",
      pho_thong: "Phổ thông",
    };
    return labelMap[hangVe] || hangVe;
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
          <Link to="/" className="nav-link">
            Trang chủ
          </Link>
          <Link to="/flights" className="nav-link">
            Danh sách chuyến bay
          </Link>
          <Link to="/about" className="nav-link">
            Về chúng tôi
          </Link>
          <Link to="/guide" className="nav-link">
            Hướng dẫn đặt vé
          </Link>
          {user ? (
            <>
              <Link to="/bookings" className="nav-link active">
                Đặt vé của tôi
              </Link>
              <span className="user-info">{user.ten_day_du || user.email}</span>
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
            <Link to="/login" className="nav-link">
              Đăng nhập
            </Link>
          )}
        </nav>
      </div>
    </header>
  );

  const renderFooter = () => (
    <footer className="customer-footer">
      <div className="footer-content">
        <div className="footer-section">
          <h3>Flight Booking</h3>
          <p>Nền tảng đặt vé máy bay hàng đầu Việt Nam</p>
          <div className="social-links">
            <a href="#" className="social-link">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
              </svg>
            </a>
            <a href="#" className="social-link">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <path d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z" />
              </svg>
            </a>
            <a href="#" className="social-link">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z" />
              </svg>
            </a>
          </div>
        </div>
        <div className="footer-section">
          <h4>Về chúng tôi</h4>
          <ul>
            <li>
              <Link to="/about">Giới thiệu</Link>
            </li>
            <li>
              <a href="#careers">Tuyển dụng</a>
            </li>
            <li>
              <a href="#news">Tin tức</a>
            </li>
            <li>
              <a href="#contact">Liên hệ</a>
            </li>
          </ul>
        </div>
        <div className="footer-section">
          <h4>Hướng dẫn</h4>
          <ul>
            <li>
              <Link to="/guide">Hướng dẫn đặt vé</Link>
            </li>
            <li>
              <a href="#payment">Thanh toán</a>
            </li>
            <li>
              <a href="#cancel">Hủy/Đổi vé</a>
            </li>
            <li>
              <a href="#baggage">Hành lý</a>
            </li>
          </ul>
        </div>
      </div>
      <div className="footer-bottom">
        <p>&copy; 2025 Flight Booking. Tất cả quyền được bảo lưu.</p>
      </div>
    </footer>
  );

  return (
    <div className="my-bookings-page">
      {renderHeader()}

      <main className="bookings-main">
        <div className="bookings-container">
          <h1 className="page-title">Đặt vé của tôi</h1>

          {error && (
            <div className="error-message" role="alert">
              {error}
            </div>
          )}

          {loading ? (
            <div className="loading-container">
              <div className="loading-spinner"></div>
              <p>Đang tải danh sách đặt vé...</p>
            </div>
          ) : bookings.length === 0 ? (
            <div className="empty-state">
              <svg
                width="64"
                height="64"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M9 11l3 3L22 4" />
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
              </svg>
              <h2>Chưa có đặt vé nào</h2>
              <p>Bạn chưa có đặt vé nào. Hãy tìm chuyến bay và đặt vé ngay!</p>
              <Link to="/flights" className="btn-primary">
                Tìm chuyến bay
              </Link>
            </div>
          ) : (
            <div className="bookings-list">
              {bookings.map((booking) => {
                // Debug: Log booking status
                console.log(
                  "Booking:",
                  booking.ma_dat_ve,
                  "Status:",
                  booking.trang_thai
                );

                return (
                  <div key={booking.id} className="booking-card">
                    <div className="booking-header">
                      <div className="booking-info">
                        <h3 className="booking-code">
                          Mã đặt vé: {booking.ma_dat_ve}
                        </h3>
                        <span
                          className={`status-badge ${getStatusClass(
                            booking.trang_thai
                          )}`}
                        >
                          {getStatusLabel(booking.trang_thai)}
                        </span>
                      </div>
                      <div className="booking-date">
                        <span className="label">Ngày đặt:</span>
                        <span>{formatDateTime(booking.created_at)}</span>
                      </div>
                    </div>

                    {(booking.chuyen_bay || booking.chuyen_bay_di) && (
                      <div className="flight-info">
                        {(() => {
                          const chuyenBay =
                            booking.chuyen_bay || booking.chuyen_bay_di;
                          return (
                            <>
                              <div className="flight-route">
                                <div className="route-item">
                                  <span className="airport-code">
                                    {
                                      chuyenBay.tuyen_bay?.san_bay_di
                                        ?.ma_san_bay
                                    }
                                  </span>
                                  <span className="airport-name">
                                    {
                                      chuyenBay.tuyen_bay?.san_bay_di
                                        ?.ten_san_bay
                                    }
                                  </span>
                                  <span className="time">
                                    {formatDateTime(chuyenBay.gio_khoi_hanh)}
                                  </span>
                                </div>
                                <div className="route-arrow">→</div>
                                <div className="route-item">
                                  <span className="airport-code">
                                    {
                                      chuyenBay.tuyen_bay?.san_bay_den
                                        ?.ma_san_bay
                                    }
                                  </span>
                                  <span className="airport-name">
                                    {
                                      chuyenBay.tuyen_bay?.san_bay_den
                                        ?.ten_san_bay
                                    }
                                  </span>
                                  <span className="time">
                                    {formatDateTime(chuyenBay.gio_ha_canh)}
                                  </span>
                                </div>
                              </div>
                              <div className="flight-meta">
                                <span>
                                  {chuyenBay.hang_hang_khong?.ten_hang} -{" "}
                                  {chuyenBay.ma_chuyen_bay}
                                </span>
                                <span>
                                  Hạng vé: {getFareClassLabel(booking.hang_ve)}
                                </span>
                              </div>
                            </>
                          );
                        })()}
                      </div>
                    )}

                    <div className="booking-details">
                      <div className="detail-row">
                        <span className="label">Số hành khách:</span>
                        <span>{booking.hanh_khach?.length || 0}</span>
                      </div>
                      <div className="detail-row">
                        <span className="label">Hạng vé:</span>
                        <span>{getFareClassLabel(booking.hang_ve)}</span>
                      </div>
                      {(() => {
                        const chuyenBay =
                          booking.chuyen_bay || booking.chuyen_bay_di;
                        if (!chuyenBay) return null;
                        return (
                          <>
                            {chuyenBay.may_bay && (
                              <div className="detail-row">
                                <span className="label">Máy bay:</span>
                                <span>
                                  {chuyenBay.may_bay.ten_may_bay ||
                                    chuyenBay.may_bay.ma_may_bay}
                                </span>
                              </div>
                            )}
                            {chuyenBay.gio_khoi_hanh &&
                              chuyenBay.gio_ha_canh && (
                                <div className="detail-row">
                                  <span className="label">Thời gian bay:</span>
                                  <span>
                                    {Math.floor(
                                      (new Date(chuyenBay.gio_ha_canh) -
                                        new Date(chuyenBay.gio_khoi_hanh)) /
                                        (1000 * 60)
                                    )}{" "}
                                    phút
                                  </span>
                                </div>
                              )}
                          </>
                        );
                      })()}
                      <div className="detail-row">
                        <span className="label">Tổng tiền:</span>
                        <span className="total-price">
                          {formatCurrency(booking.tong_tien)}
                        </span>
                      </div>
                      {(booking.thoi_gian_het_han ||
                        booking.thoi_gian_het_han_giu_cho) && (
                        <div className="detail-row">
                          <span className="label">Hết hạn thanh toán:</span>
                          <span className="expiry-time">
                            {formatDateTime(
                              booking.thoi_gian_het_han ||
                                booking.thoi_gian_het_han_giu_cho
                            )}
                          </span>
                        </div>
                      )}
                    </div>

                    {/* Thông tin hành khách */}
                    {booking.hanh_khach && booking.hanh_khach.length > 0 && (
                      <div className="passengers-info">
                        <h4 className="passengers-title">Hành khách:</h4>
                        <div className="passengers-list">
                          {booking.hanh_khach.map((passenger, idx) => (
                            <div key={idx} className="passenger-item">
                              <span className="passenger-name">
                                {passenger.ho_ten}
                              </span>
                              <span className="passenger-type">
                                {getPassengerTypeLabel(
                                  passenger.loai_hanh_khach
                                )}
                              </span>
                              {passenger.so_ghe && (
                                <span className="passenger-seat">
                                  Ghế: {passenger.so_ghe}
                                </span>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    <div className="booking-actions">
                      <button
                        className="btn-detail"
                        onClick={() => handleViewDetail(booking.id)}
                        disabled={loadingDetail}
                      >
                        {loadingDetail && selectedBooking?.id === booking.id
                          ? "Đang tải..."
                          : "Chi tiết"}
                      </button>
                      {/* Hiển thị nút thanh toán và hủy cho tất cả trạng thái chờ thanh toán */}
                      {(booking.trang_thai === "cho_thanh_toan" ||
                        booking.trang_thai === "chờ_thanh_toan" ||
                        booking.trang_thai === "giu_cho") && (
                        <>
                          <button
                            className="btn-payment"
                            onClick={() => handlePayment(booking.id)}
                            disabled={processingPayment === booking.id}
                          >
                            {processingPayment === booking.id ? (
                              <>Đang xử lý...</>
                            ) : (
                              <>Thanh toán</>
                            )}
                          </button>
                          <button
                            className="btn-cancel"
                            onClick={() => handleCancel(booking.id)}
                            disabled={cancelling === booking.id}
                          >
                            {cancelling === booking.id ? "Đang hủy..." : "Hủy"}
                          </button>
                        </>
                      )}
                      {booking.trang_thai === "da_thanh_toan" && (
                        <div className="payment-info">
                          <span>
                            Đã thanh toán vào:{" "}
                            {booking.thoi_gian_thanh_toan
                              ? formatDateTime(booking.thoi_gian_thanh_toan)
                              : "N/A"}
                          </span>
                          {booking.ma_giao_dich && (
                            <span className="transaction-code">
                              Mã giao dịch: {booking.ma_giao_dich}
                            </span>
                          )}
                        </div>
                      )}
                      {booking.trang_thai === "da_huy" && (
                        <span className="cancelled-info">
                          Đặt vé đã được hủy
                        </span>
                      )}
                      {/* Fallback: Nếu không có trạng thái nào khớp, vẫn hiển thị nút nếu chưa thanh toán */}
                      {![
                        "da_thanh_toan",
                        "da_huy",
                        "cho_thanh_toan",
                        "chờ_thanh_toan",
                        "giu_cho",
                      ].includes(booking.trang_thai) && (
                        <>
                          <button
                            className="btn-payment"
                            onClick={() => handlePayment(booking.id)}
                            disabled={processingPayment === booking.id}
                          >
                            {processingPayment === booking.id ? (
                              <>Đang xử lý...</>
                            ) : (
                              <>Thanh toán</>
                            )}
                          </button>
                          <button
                            className="btn-cancel"
                            onClick={() => handleCancel(booking.id)}
                            disabled={cancelling === booking.id}
                          >
                            {cancelling === booking.id ? "Đang hủy..." : "Hủy"}
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </main>

      {renderFooter()}

      {/* Modal Chi tiết đặt vé */}
      {showDetailModal && selectedBooking && (
        <div
          className="modal-overlay"
          onClick={() => setShowDetailModal(false)}
        >
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Chi tiết đặt vé</h2>
              <button
                className="modal-close"
                onClick={() => setShowDetailModal(false)}
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <div className="detail-section">
                <h3>Thông tin đặt vé</h3>
                <div className="detail-grid">
                  <div className="detail-item">
                    <span className="detail-label">Mã đặt vé:</span>
                    <span className="detail-value">
                      {selectedBooking.ma_dat_ve}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Trạng thái:</span>
                    <span
                      className={`status-badge ${getStatusClass(
                        selectedBooking.trang_thai
                      )}`}
                    >
                      {getStatusLabel(selectedBooking.trang_thai)}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Ngày đặt:</span>
                    <span className="detail-value">
                      {formatDateTime(selectedBooking.created_at)}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Tổng tiền:</span>
                    <span className="detail-value total-price">
                      {formatCurrency(selectedBooking.tong_tien)}
                    </span>
                  </div>
                </div>
              </div>

              {(() => {
                const chuyenBay =
                  selectedBooking.chuyen_bay || selectedBooking.chuyen_bay_di;
                if (!chuyenBay) return null;
                return (
                  <div className="detail-section">
                    <h3>Thông tin chuyến bay</h3>
                    <div className="detail-grid">
                      <div className="detail-item">
                        <span className="detail-label">Mã chuyến bay:</span>
                        <span className="detail-value">
                          {chuyenBay.ma_chuyen_bay}
                        </span>
                      </div>
                      <div className="detail-item">
                        <span className="detail-label">Hãng hàng không:</span>
                        <span className="detail-value">
                          {chuyenBay.hang_hang_khong?.ten_hang}
                        </span>
                      </div>
                      {chuyenBay.may_bay && (
                        <div className="detail-item">
                          <span className="detail-label">Máy bay:</span>
                          <span className="detail-value">
                            {chuyenBay.may_bay.ten_may_bay ||
                              chuyenBay.may_bay.ma_may_bay}
                          </span>
                        </div>
                      )}
                      <div className="detail-item">
                        <span className="detail-label">Sân bay đi:</span>
                        <span className="detail-value">
                          {chuyenBay.tuyen_bay?.san_bay_di?.ten_san_bay} (
                          {chuyenBay.tuyen_bay?.san_bay_di?.ma_san_bay})
                        </span>
                      </div>
                      <div className="detail-item">
                        <span className="detail-label">Sân bay đến:</span>
                        <span className="detail-value">
                          {chuyenBay.tuyen_bay?.san_bay_den?.ten_san_bay} (
                          {chuyenBay.tuyen_bay?.san_bay_den?.ma_san_bay})
                        </span>
                      </div>
                      <div className="detail-item">
                        <span className="detail-label">Giờ khởi hành:</span>
                        <span className="detail-value">
                          {formatDateTime(chuyenBay.gio_khoi_hanh)}
                        </span>
                      </div>
                      <div className="detail-item">
                        <span className="detail-label">Giờ hạ cánh:</span>
                        <span className="detail-value">
                          {formatDateTime(chuyenBay.gio_ha_canh)}
                        </span>
                      </div>
                      {chuyenBay.gio_khoi_hanh && chuyenBay.gio_ha_canh && (
                        <div className="detail-item">
                          <span className="detail-label">Thời gian bay:</span>
                          <span className="detail-value">
                            {Math.floor(
                              (new Date(chuyenBay.gio_ha_canh) -
                                new Date(chuyenBay.gio_khoi_hanh)) /
                                (1000 * 60)
                            )}{" "}
                            phút
                          </span>
                        </div>
                      )}
                      <div className="detail-item">
                        <span className="detail-label">Hạng vé:</span>
                        <span className="detail-value">
                          {getFareClassLabel(selectedBooking.hang_ve)}
                        </span>
                      </div>
                    </div>
                  </div>
                );
              })()}

              {selectedBooking.hanh_khach &&
                selectedBooking.hanh_khach.length > 0 && (
                  <div className="detail-section">
                    <h3>Thông tin hành khách</h3>
                    <div className="passengers-detail-list">
                      {selectedBooking.hanh_khach.map((passenger, idx) => (
                        <div key={idx} className="passenger-detail-item">
                          <div className="passenger-detail-header">
                            <span className="passenger-detail-name">
                              {passenger.ho_ten}
                            </span>
                            <span className="passenger-detail-type">
                              {getPassengerTypeLabel(passenger.loai_hanh_khach)}
                            </span>
                          </div>
                          <div className="passenger-detail-info">
                            {passenger.so_ghe && (
                              <div className="passenger-detail-row">
                                <span className="label">Số ghế:</span>
                                <span>{passenger.so_ghe}</span>
                              </div>
                            )}
                            {passenger.so_ho_chieu && (
                              <div className="passenger-detail-row">
                                <span className="label">Số hộ chiếu:</span>
                                <span>{passenger.so_ho_chieu}</span>
                              </div>
                            )}
                            {passenger.loai_giay_to && (
                              <div className="passenger-detail-row">
                                <span className="label">Loại giấy tờ:</span>
                                <span>
                                  {passenger.loai_giay_to === "can_cuoc"
                                    ? "Căn cước"
                                    : "Hộ chiếu"}
                                </span>
                              </div>
                            )}
                            {passenger.so_giay_to && (
                              <div className="passenger-detail-row">
                                <span className="label">Số giấy tờ:</span>
                                <span>{passenger.so_giay_to}</span>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

              {(selectedBooking.thoi_gian_het_han_giu_cho ||
                selectedBooking.thoi_gian_thanh_toan) && (
                <div className="detail-section">
                  <h3>Thông tin thanh toán</h3>
                  <div className="detail-grid">
                    {selectedBooking.thoi_gian_het_han_giu_cho && (
                      <div className="detail-item">
                        <span className="detail-label">
                          Hết hạn thanh toán:
                        </span>
                        <span className="detail-value expiry-time">
                          {formatDateTime(
                            selectedBooking.thoi_gian_het_han_giu_cho
                          )}
                        </span>
                      </div>
                    )}
                    {selectedBooking.thoi_gian_thanh_toan && (
                      <div className="detail-item">
                        <span className="detail-label">
                          Thời gian thanh toán:
                        </span>
                        <span className="detail-value">
                          {formatDateTime(selectedBooking.thoi_gian_thanh_toan)}
                        </span>
                      </div>
                    )}
                    {selectedBooking.ma_giao_dich && (
                      <div className="detail-item">
                        <span className="detail-label">Mã giao dịch:</span>
                        <span className="detail-value">
                          {selectedBooking.ma_giao_dich}
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button
                className="btn-close-modal"
                onClick={() => setShowDetailModal(false)}
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
