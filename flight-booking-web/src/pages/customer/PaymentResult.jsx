import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { getBooking, confirmPayment } from "../../api/customer";
import "../../styles/customerHome.css";

export default function PaymentResult() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);

  // Lấy tham số từ URL - có thể từ backend redirect hoặc trực tiếp từ VNPAY
  const status = searchParams.get("status");
  const bookingId = searchParams.get("booking_id");
  const transactionNo = searchParams.get("transaction_no");
  const message = searchParams.get("message");

  // Nếu không có status từ backend, kiểm tra tham số VNPAY trực tiếp
  const vnpResponseCode = searchParams.get("vnp_ResponseCode");
  const vnpTxnRef = searchParams.get("vnp_TxnRef");
  const vnpTransactionNo = searchParams.get("vnp_TransactionNo");
  const vnpSecureHash = searchParams.get("vnp_SecureHash");

  useEffect(() => {
    // Log để debug
    console.log(
      "PaymentResult - Status:",
      status,
      "BookingId:",
      bookingId,
      "TransactionNo:",
      transactionNo
    );
    console.log("PaymentResult - VNPAY params:", {
      vnpResponseCode,
      vnpTxnRef,
      vnpTransactionNo,
      allParams: Object.fromEntries(searchParams.entries()),
    });

    // Nếu có tham số từ backend redirect
    if (bookingId && status === "success") {
      loadBooking();
    }
    // Nếu có tham số VNPAY trực tiếp (VNPAY redirect về frontend)
    else if (vnpResponseCode && vnpTxnRef) {
      // Gọi API để xử lý và lấy thông tin booking
      processVNPayCallback();
    } else {
      setLoading(false);
    }
  }, [bookingId, status, vnpResponseCode, vnpTxnRef]);

  const loadBooking = async (id) => {
    try {
      const response = await getBooking(id || bookingId);
      setBooking(response.data);
    } catch (err) {
      console.error("Error loading booking:", err);
    } finally {
      setLoading(false);
    }
  };

  const processVNPayCallback = async () => {
    try {
      setLoading(true);

      // Extract booking ID từ vnp_TxnRef (format: {booking_id}_{timestamp}_{random})
      let extractedBookingId = null;
      if (vnpTxnRef) {
        const parts = vnpTxnRef.split("_");
        if (parts[0] && !isNaN(parts[0])) {
          extractedBookingId = parseInt(parts[0]);
        } else if (!isNaN(vnpTxnRef)) {
          extractedBookingId = parseInt(vnpTxnRef);
        }
      }

      if (!extractedBookingId) {
        console.error("Cannot extract booking ID from vnp_TxnRef:", vnpTxnRef);
        setLoading(false);
        return;
      }

      // Kiểm tra response code
      if (vnpResponseCode === "00" || vnpResponseCode === "07") {
        // Thanh toán thành công - gọi API để cập nhật trạng thái
        try {
          // Gọi API để xác nhận thanh toán và cập nhật trạng thái
          await confirmPayment(extractedBookingId, {
            vnp_ResponseCode: vnpResponseCode,
            vnp_TxnRef: vnpTxnRef,
            vnp_TransactionNo: vnpTransactionNo,
            vnp_SecureHash: vnpSecureHash,
            // Gửi tất cả tham số VNPAY để backend xác thực
            ...Object.fromEntries(searchParams.entries()),
          });
        } catch (err) {
          console.error("Error confirming payment:", err);
          // Vẫn tiếp tục load booking để hiển thị thông tin
        }

        // Load booking info sau khi cập nhật
        await loadBooking(extractedBookingId);
      } else {
        // Thanh toán thất bại - vẫn load booking để hiển thị thông tin
        try {
          await loadBooking(extractedBookingId);
        } catch (err) {
          console.error("Error loading booking for failed payment:", err);
        }
        setLoading(false);
      }
    } catch (err) {
      console.error("Error processing VNPAY callback:", err);
      setLoading(false);
    }
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
              <Link to="/bookings" className="nav-link">
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
          <h4>Liên hệ</h4>
          <p>Email: support@flightbooking.com</p>
          <p>Hotline: 1900 1234</p>
          <p>Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM</p>
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

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleString("vi-VN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (loading) {
    return (
      <div className="customer-home">
        {renderHeader()}
        <main className="static-page">
          <div className="static-page-container">
            <div style={{ textAlign: "center", padding: "40px" }}>
              <div className="loading-spinner"></div>
              <p>Đang tải thông tin...</p>
            </div>
          </div>
        </main>
        {renderFooter()}
      </div>
    );
  }

  return (
    <div className="customer-home">
      {renderHeader()}
      <main className="static-page">
        <div className="static-page-container">
          {status === "success" ||
          vnpResponseCode === "00" ||
          vnpResponseCode === "07" ? (
            <>
              <div style={{ textAlign: "center", marginBottom: "40px" }}>
                <div
                  style={{
                    width: "80px",
                    height: "80px",
                    borderRadius: "50%",
                    background: "#10b981",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    margin: "0 auto 20px",
                  }}
                >
                  <svg
                    width="40"
                    height="40"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="white"
                    strokeWidth="3"
                  >
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                </div>
                <h1 className="static-page-title" style={{ color: "#10b981" }}>
                  Thanh toán thành công!
                </h1>
                <p
                  style={{
                    fontSize: "18px",
                    color: "#475569",
                    marginTop: "10px",
                  }}
                >
                  Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi
                </p>
              </div>

              {booking && (
                <div
                  className="notice-box"
                  style={{ background: "#f0fdf4", borderColor: "#10b981" }}
                >
                  <h3 style={{ color: "#059669" }}>Thông tin đặt vé</h3>
                  <div style={{ marginTop: "16px" }}>
                    <p>
                      <strong>Mã đặt vé:</strong> {booking.ma_dat_ve}
                    </p>
                    {(transactionNo || vnpTransactionNo) && (
                      <p>
                        <strong>Mã giao dịch:</strong>{" "}
                        {transactionNo || vnpTransactionNo}
                      </p>
                    )}
                    {booking.chuyen_bay && (
                      <>
                        <p>
                          <strong>Chuyến bay:</strong>{" "}
                          {booking.chuyen_bay.ma_chuyen_bay}
                        </p>
                        <p>
                          <strong>Tổng tiền:</strong>{" "}
                          {formatCurrency(booking.tong_tien)}
                        </p>
                      </>
                    )}
                  </div>
                </div>
              )}

              <div style={{ textAlign: "center", marginTop: "40px" }}>
                <Link
                  to="/bookings"
                  style={{
                    display: "inline-block",
                    padding: "12px 32px",
                    background:
                      "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                    color: "white",
                    textDecoration: "none",
                    borderRadius: "8px",
                    fontWeight: "600",
                    marginRight: "16px",
                  }}
                >
                  Xem đặt vé của tôi
                </Link>
                <Link
                  to="/"
                  style={{
                    display: "inline-block",
                    padding: "12px 32px",
                    background: "#f1f5f9",
                    color: "#475569",
                    textDecoration: "none",
                    borderRadius: "8px",
                    fontWeight: "600",
                  }}
                >
                  Về trang chủ
                </Link>
              </div>
            </>
          ) : (
            <>
              <div style={{ textAlign: "center", marginBottom: "40px" }}>
                <div
                  style={{
                    width: "80px",
                    height: "80px",
                    borderRadius: "50%",
                    background: "#ef4444",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    margin: "0 auto 20px",
                  }}
                >
                  <svg
                    width="40"
                    height="40"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="white"
                    strokeWidth="3"
                  >
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </div>
                <h1 className="static-page-title" style={{ color: "#ef4444" }}>
                  Thanh toán thất bại
                </h1>
                <p
                  style={{
                    fontSize: "18px",
                    color: "#475569",
                    marginTop: "10px",
                  }}
                >
                  {message || "Có lỗi xảy ra trong quá trình thanh toán"}
                </p>
              </div>

              <div className="notice-box">
                <h3>Lưu ý</h3>
                <p>
                  Nếu bạn đã thanh toán nhưng nhận được thông báo này, vui lòng
                  liên hệ với chúng tôi qua hotline 1900 1234 hoặc email
                  support@flightbooking.com với mã đặt vé của bạn.
                </p>
              </div>

              <div style={{ textAlign: "center", marginTop: "40px" }}>
                {(bookingId || vnpTxnRef || booking) && (
                  <Link
                    to={`/bookings`}
                    style={{
                      display: "inline-block",
                      padding: "12px 32px",
                      background:
                        "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                      color: "white",
                      textDecoration: "none",
                      borderRadius: "8px",
                      fontWeight: "600",
                      marginRight: "16px",
                    }}
                  >
                    Xem đặt vé
                  </Link>
                )}
                <Link
                  to="/"
                  style={{
                    display: "inline-block",
                    padding: "12px 32px",
                    background: "#f1f5f9",
                    color: "#475569",
                    textDecoration: "none",
                    borderRadius: "8px",
                    fontWeight: "600",
                  }}
                >
                  Về trang chủ
                </Link>
              </div>
            </>
          )}
        </div>
      </main>
      {renderFooter()}
    </div>
  );
}
