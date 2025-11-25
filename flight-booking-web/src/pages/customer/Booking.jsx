import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {
  createBooking,
  getFlightDetail,
  getFlightSeats,
  createPaymentUrl,
} from "../../api/customer";
import { useAuth } from "../../auth/AuthContext";
import SeatMap from "../../components/SeatMap";
import "../../styles/booking.css";

export default function Booking() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [flight, setFlight] = useState(null);
  const [fareClass, setFareClass] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [seatData, setSeatData] = useState(null);
  const [loadingSeats, setLoadingSeats] = useState(false);
  const [selectedSeats, setSelectedSeats] = useState([]);

  // Form state
  const [passengers, setPassengers] = useState([
    {
      ho_ten: "",
      so_ho_chieu: "",
      loai_giay_to: "ho_chieu",
      so_giay_to: "",
      so_ghe: "",
      gia_ghe: null, // Giá của ghế đã chọn
      loai_hanh_khach: "nguoi_lon",
    },
  ]);
  const [contactInfo, setContactInfo] = useState({
    email: user?.email || "",
    so_dien_thoai: "",
    ten_day_du: user?.ten || "",
  });

  useEffect(() => {
    // Check authentication first
    const token = localStorage.getItem("fb_token");
    if (!user || !token) {
      setError("Vui lòng đăng nhập để đặt vé");
      setTimeout(() => {
        navigate("/login", {
          state: {
            from: location.pathname,
            message: "Vui lòng đăng nhập để đặt vé",
          },
        });
      }, 1000);
      setLoading(false);
      return;
    }

    // Check user role
    if (user.vai_tro !== "khach_hang") {
      setError(
        "Bạn không có quyền đặt vé. Vui lòng đăng nhập bằng tài khoản khách hàng."
      );
      setLoading(false);
      return;
    }

    // Get flight from location state
    if (location.state?.flight) {
      const flightData = location.state.flight;
      setFlight(flightData);

      // If fareClass is provided, use it
      if (location.state?.fareClass) {
        setFareClass(location.state.fareClass);
        setLoading(false);
        loadSeatData(flightData.id);
      } else {
        // Load flight detail to get fare classes
        loadFlightDetail(flightData.id);
      }
    } else {
      setError("Thông tin chuyến bay không hợp lệ");
      setLoading(false);
    }
  }, [location.state, user, navigate]);

  const loadFlightDetail = async (flightId) => {
    try {
      setLoading(true);
      setError(null);
      const response = await getFlightDetail(flightId);
      const flightDetail = response.data;

      // Update flight with full details
      setFlight(flightDetail);

      // Auto-select first fare class if available
      if (flightDetail.gia_ve && flightDetail.gia_ve.length > 0) {
        setFareClass(flightDetail.gia_ve[0]);
      } else {
        // If no fare classes, create a default one based on tong_gia
        setFareClass({
          hang_ve: "pho_thong",
          gia: flightDetail.tong_gia || 0,
        });
      }

      // Load seat data
      loadSeatData(flightId);
    } catch (err) {
      console.error("Error loading flight detail:", err);
      setError("Không thể tải thông tin chuyến bay. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const loadSeatData = async (flightId) => {
    try {
      setLoadingSeats(true);
      const response = await getFlightSeats(flightId);
      setSeatData(response.data);
    } catch (err) {
      console.error("Error loading seat data:", err);
      // Không block nếu không load được seat data
    } finally {
      setLoadingSeats(false);
    }
  };

  const formatCurrency = (amount) => {
    const numAmount = Number(amount);
    if (isNaN(numAmount) || numAmount < 0) {
      return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
      }).format(0);
    }
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(numAmount);
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

  const getFareClassLabel = (hangVe) => {
    const labels = {
      pho_thong: "Phổ thông",
      pho_thong_cao_cap: "Phổ thông cao cấp",
      thuong_gia: "Thương gia",
      hang_nhat: "Hạng nhất",
    };
    return labels[hangVe] || hangVe;
  };

  const calculatePassengerPrice = (passenger) => {
    // Sử dụng giá ghế nếu đã chọn ghế, nếu không thì dùng giá hạng vé
    let basePrice = 0;

    if (
      passenger.gia_ghe !== null &&
      passenger.gia_ghe !== undefined &&
      passenger.gia_ghe > 0
    ) {
      basePrice = Number(passenger.gia_ghe);
    } else if (fareClass && fareClass.gia) {
      basePrice = Number(fareClass.gia);
    }

    if (!basePrice || isNaN(basePrice) || basePrice <= 0) return 0;

    let finalPrice = 0;
    switch (passenger.loai_hanh_khach) {
      case "nguoi_lon":
        finalPrice = basePrice;
        break;
      case "tre_em":
        finalPrice = basePrice * 0.75; // 75% giá người lớn
        break;
      case "em_be":
        finalPrice = basePrice * 0.1; // 10% giá người lớn
        break;
      default:
        finalPrice = basePrice;
    }

    return isNaN(finalPrice) ? 0 : Math.round(finalPrice);
  };

  const calculateTotalPrice = () => {
    // Không cần kiểm tra fareClass vì calculatePassengerPrice đã xử lý
    let total = 0;

    for (let i = 0; i < passengers.length; i++) {
      const passenger = passengers[i];
      const price = calculatePassengerPrice(passenger);
      const priceNum = typeof price === "number" && !isNaN(price) ? price : 0;
      total += priceNum;
    }

    return typeof total === "number" && !isNaN(total) ? total : 0;
  };

  const handlePassengerChange = (index, field, value) => {
    const updated = [...passengers];
    updated[index][field] = value;
    setPassengers(updated);

    // Update selected seats list and seat price
    if (field === "so_ghe") {
      const currentSeats = passengers.map((p) => p.so_ghe).filter(Boolean);
      const newSeats = updated.map((p) => p.so_ghe).filter(Boolean);
      setSelectedSeats(newSeats);

      // Nếu xóa số ghế, cũng xóa giá ghế
      if (!value) {
        updated[index].gia_ghe = null;
      }
    }
  };

  const handleSeatSelect = (seat) => {
    // Tìm hành khách chưa có ghế
    const passengerIndex = passengers.findIndex((p) => !p.so_ghe);

    if (passengerIndex === -1) {
      // Tất cả hành khách đã có ghế, cho phép đổi ghế
      // Tìm ghế đang được chọn và xóa nó
      const currentSelected = selectedSeats;
      if (currentSelected.includes(seat.number)) {
        // Đã chọn rồi, bỏ chọn
        const newSelected = selectedSeats.filter((s) => s !== seat.number);
        setSelectedSeats(newSelected);
        const updated = passengers.map((p) => {
          if (p.so_ghe === seat.number) {
            return { ...p, so_ghe: "", gia_ghe: null };
          }
          return p;
        });
        setPassengers(updated);
      } else {
        // Chọn ghế mới - gán cho hành khách đầu tiên chưa có ghế hoặc hành khách đầu tiên
        const updated = [...passengers];
        if (updated[0].so_ghe) {
          // Đổi ghế cho hành khách đầu tiên
          const oldSeat = updated[0].so_ghe;
          updated[0].so_ghe = seat.number;
          updated[0].gia_ghe = seat.price || null;
          setSelectedSeats([
            ...selectedSeats.filter((s) => s !== oldSeat),
            seat.number,
          ]);
        } else {
          updated[0].so_ghe = seat.number;
          updated[0].gia_ghe = seat.price || null;
          setSelectedSeats([...selectedSeats, seat.number]);
        }
        setPassengers(updated);
      }
    } else {
      // Gán ghế cho hành khách chưa có ghế
      const updated = [...passengers];
      updated[passengerIndex].so_ghe = seat.number;
      updated[passengerIndex].gia_ghe = seat.price || null;
      setPassengers(updated);
      setSelectedSeats([...selectedSeats, seat.number]);
    }
  };

  const handleContactChange = (field, value) => {
    setContactInfo({ ...contactInfo, [field]: value });
  };

  const addPassenger = () => {
    setPassengers([
      ...passengers,
      {
        ho_ten: "",
        so_ho_chieu: "",
        loai_giay_to: "ho_chieu",
        so_giay_to: "",
        so_ghe: "",
        gia_ghe: null,
        loai_hanh_khach: "nguoi_lon",
      },
    ]);
  };

  const removePassenger = (index) => {
    if (passengers.length > 1) {
      setPassengers(passengers.filter((_, i) => i !== index));
    }
  };

  const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const validateForm = () => {
    // Validate contact info
    if (
      !contactInfo.email ||
      !contactInfo.so_dien_thoai ||
      !contactInfo.ten_day_du
    ) {
      return "Vui lòng điền đầy đủ thông tin liên hệ";
    }

    // Validate email format
    if (!validateEmail(contactInfo.email)) {
      return "Email không hợp lệ. Vui lòng nhập đúng định dạng email (ví dụ: example@email.com)";
    }

    // Validate phone number (basic)
    if (contactInfo.so_dien_thoai.length < 10) {
      return "Số điện thoại phải có ít nhất 10 số";
    }

    // Validate passengers
    for (let i = 0; i < passengers.length; i++) {
      const p = passengers[i];
      if (!p.ho_ten || p.ho_ten.trim() === "") {
        return `Vui lòng nhập họ tên hành khách ${i + 1}`;
      }
      if (
        p.loai_hanh_khach === "nguoi_lon" &&
        (!p.so_giay_to || p.so_giay_to.trim() === "")
      ) {
        return `Vui lòng nhập số giấy tờ cho hành khách ${i + 1}`;
      }
    }

    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    // Check authentication
    const token = localStorage.getItem("fb_token");
    if (!user || !token) {
      setError("Vui lòng đăng nhập để đặt vé");
      setTimeout(() => {
        navigate("/login", {
          state: {
            from: location.pathname,
            message: "Vui lòng đăng nhập để đặt vé",
          },
        });
      }, 1500);
      return;
    }

    // Check user role
    if (user.vai_tro !== "khach_hang") {
      setError(
        "Bạn không có quyền đặt vé. Vui lòng đăng nhập bằng tài khoản khách hàng."
      );
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      // Ensure we have fareClass
      if (!fareClass || !fareClass.hang_ve) {
        setError("Vui lòng chọn hạng vé");
        return;
      }

      // Reload seat data để đảm bảo có dữ liệu mới nhất
      if (flight?.id) {
        try {
          const seatResponse = await getFlightSeats(flight.id);
          const latestSeatData = seatResponse.data;
          
          // Kiểm tra xem ghế đã chọn có còn trống không
          const bookedSeats = latestSeatData?.ghe_da_dat || [];
          const reservedSeats = latestSeatData?.ghe_giu_cho || [];
          const allOccupiedSeats = [...bookedSeats, ...reservedSeats];
          
          const selectedSeatNumbers = passengers
            .map((p) => p.so_ghe?.trim())
            .filter(Boolean);
          
          const occupiedSelectedSeats = selectedSeatNumbers.filter((seat) =>
            allOccupiedSeats.some((occupied) => 
              occupied?.trim() === seat || occupied === seat
            )
          );
          
          if (occupiedSelectedSeats.length > 0) {
            setError(
              `Các ghế sau đã được đặt hoặc đang được giữ chỗ: ${occupiedSelectedSeats.join(", ")}. Vui lòng chọn ghế khác.`
            );
            setSubmitting(false);
            // Reload seat map với dữ liệu mới
            setSeatData(latestSeatData);
            return;
          }
          
          // Cập nhật seat data với dữ liệu mới nhất
          setSeatData(latestSeatData);
        } catch (seatErr) {
          console.error("Error reloading seat data:", seatErr);
          // Tiếp tục với dữ liệu cũ nếu không load được
        }
      }

      const bookingData = {
        ma_chuyen_bay_di: flight.id,
        ma_chuyen_bay_ve: location.state?.selectedFlightVe?.id || null,
        hang_ve: fareClass.hang_ve,
        hanh_khach: passengers.map((p) => ({
          ho_ten: p.ho_ten,
          so_ho_chieu: p.so_ho_chieu,
          loai_giay_to: p.loai_giay_to,
          so_giay_to: p.so_giay_to,
          so_ghe: p.so_ghe || null,
          loai_hanh_khach: p.loai_hanh_khach,
        })),
        thong_tin_lien_he: contactInfo,
      };

      const response = await createBooking(bookingData);

      // Đặt vé thành công, redirect đến trang "Vé của tôi"
      navigate("/bookings", {
        state: {
          message:
            "Đặt vé thành công! Bạn có thể thanh toán hoặc hủy vé trong trang 'Vé của tôi'.",
          booking: response.data,
        },
      });
    } catch (err) {
      console.error("Error creating booking:", err);

      // Handle different error cases
      if (err.response?.status === 401) {
        setError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        setTimeout(() => {
          localStorage.removeItem("fb_token");
          localStorage.removeItem("fb_user");
          navigate("/login", {
            state: {
              from: location.pathname,
              message: "Phiên đăng nhập đã hết hạn",
            },
          });
        }, 2000);
      } else if (err.response?.status === 403) {
        setError(
          "Bạn không có quyền đặt vé. Vui lòng đăng nhập bằng tài khoản khách hàng."
        );
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data?.errors) {
        // Handle validation errors
        const errorMessages = Object.values(err.response.data.errors).flat();
        setError(errorMessages.join(", "));
      } else {
        setError("Có lỗi xảy ra khi đặt vé. Vui lòng thử lại.");
      }
    } finally {
      setSubmitting(false);
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
          <a href="/" className="nav-link">
            Trang chủ
          </a>
          <a href="/flights" className="nav-link">
            Danh sách chuyến bay
          </a>
          {user ? (
            <>
              <a href="/bookings" className="nav-link">
                Đặt vé của tôi
              </a>
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
      <div className="booking-page">
        {renderHeader()}
        <main className="booking-main">
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải thông tin...</p>
          </div>
        </main>
        {renderFooter()}
      </div>
    );
  }

  if (error && !flight) {
    return (
      <div className="booking-page">
        {renderHeader()}
        <main className="booking-main">
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
            <h3>Không tìm thấy thông tin chuyến bay</h3>
            <p>{error}</p>
            <button className="btn-retry" onClick={() => navigate("/")}>
              Về trang chủ
            </button>
          </div>
        </main>
        {renderFooter()}
      </div>
    );
  }

  if (!flight) {
    return null;
  }

  // If no fareClass yet but flight is loaded, show loading or allow selection
  if (!fareClass) {
    return (
      <div className="booking-page">
        {renderHeader()}
        <main className="booking-main">
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải thông tin hạng vé...</p>
          </div>
        </main>
        {renderFooter()}
      </div>
    );
  }

  return (
    <div className="booking-page">
      {renderHeader()}

      <main className="booking-main">
        <div className="booking-container">
          <div className="booking-content">
            {/* Flight Summary */}
            <div className="flight-summary-card">
              <h2 className="section-title">Thông tin chuyến bay</h2>
              <div className="flight-summary">
                <div className="summary-route">
                  <div className="route-point">
                    <div className="airport-code">
                      {flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                    </div>
                    <div className="airport-name">
                      {flight.tuyen_bay?.san_bay_di?.ten_san_bay}
                    </div>
                    <div className="route-time">
                      {formatTime(flight.gio_khoi_hanh)}
                    </div>
                  </div>
                  <div className="route-arrow">→</div>
                  <div className="route-point">
                    <div className="airport-code">
                      {flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                    </div>
                    <div className="airport-name">
                      {flight.tuyen_bay?.san_bay_den?.ten_san_bay}
                    </div>
                    <div className="route-time">
                      {formatTime(flight.gio_ha_canh)}
                    </div>
                  </div>
                </div>
                <div className="summary-details">
                  <div className="detail-item">
                    <span className="detail-label">Ngày bay:</span>
                    <span className="detail-value">
                      {formatDate(flight.gio_khoi_hanh)}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Hãng hàng không:</span>
                    <span className="detail-value">
                      {flight.hang_hang_khong?.ten_hang || "N/A"}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Fare Class Selection */}
            {flight.gia_ve && flight.gia_ve.length > 1 && (
              <div className="form-section">
                <h3>Chọn hạng vé</h3>
                <div className="fare-classes-selection">
                  {flight.gia_ve.map((fare) => (
                    <div
                      key={fare.id}
                      className={`fare-class-card ${
                        fareClass?.id === fare.id ? "selected" : ""
                      }`}
                      onClick={() => setFareClass(fare)}
                    >
                      <div className="fare-class-header">
                        <div className="fare-class-name">
                          {getFareClassLabel(fare.hang_ve)}
                        </div>
                        <div className="fare-class-price">
                          {formatCurrency(fare.gia)}
                        </div>
                      </div>
                      {fare.hanh_ly_ky_gui && (
                        <div className="fare-class-detail">
                          Hành lý ký gửi: {fare.hanh_ly_ky_gui}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Booking Form */}
            <form onSubmit={handleSubmit} className="booking-form">
              {/* Passengers Section */}
              <div className="form-section">
                <div className="section-header">
                  <h3>Thông tin hành khách</h3>
                  <button
                    type="button"
                    className="btn-add-passenger"
                    onClick={addPassenger}
                  >
                    <svg
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <line x1="12" y1="5" x2="12" y2="19"></line>
                      <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    Thêm hành khách
                  </button>
                </div>

                {passengers.map((passenger, index) => (
                  <div key={index} className="passenger-card">
                    <div className="passenger-header">
                      <h4>Hành khách {index + 1}</h4>
                      {passengers.length > 1 && (
                        <button
                          type="button"
                          className="btn-remove"
                          onClick={() => removePassenger(index)}
                        >
                          <svg
                            width="18"
                            height="18"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                          >
                            <line x1="18" y1="6" x2="6" y2="18"></line>
                            <line x1="6" y1="6" x2="18" y2="18"></line>
                          </svg>
                        </button>
                      )}
                    </div>

                    <div className="form-grid">
                      <div className="form-group">
                        <label>
                          Họ và tên <span className="required">*</span>
                        </label>
                        <input
                          type="text"
                          value={passenger.ho_ten}
                          onChange={(e) =>
                            handlePassengerChange(
                              index,
                              "ho_ten",
                              e.target.value
                            )
                          }
                          required
                          placeholder="Nhập họ và tên"
                        />
                      </div>

                      <div className="form-group">
                        <label>Loại hành khách</label>
                        <select
                          value={passenger.loai_hanh_khach}
                          onChange={(e) =>
                            handlePassengerChange(
                              index,
                              "loai_hanh_khach",
                              e.target.value
                            )
                          }
                        >
                          <option value="nguoi_lon">
                            Người lớn (12+ tuổi)
                          </option>
                          <option value="tre_em">Trẻ em (2-11 tuổi)</option>
                          <option value="em_be">Em bé (dưới 2 tuổi)</option>
                        </select>
                      </div>

                      <div className="form-group">
                        <label>Loại giấy tờ</label>
                        <select
                          value={passenger.loai_giay_to}
                          onChange={(e) =>
                            handlePassengerChange(
                              index,
                              "loai_giay_to",
                              e.target.value
                            )
                          }
                        >
                          <option value="ho_chieu">Hộ chiếu</option>
                          <option value="can_cuoc">Căn cước công dân</option>
                        </select>
                      </div>

                      <div className="form-group">
                        <label>
                          Số giấy tờ{" "}
                          {passenger.loai_hanh_khach === "nguoi_lon" && (
                            <span className="required">*</span>
                          )}
                        </label>
                        <input
                          type="text"
                          value={passenger.so_giay_to}
                          onChange={(e) =>
                            handlePassengerChange(
                              index,
                              "so_giay_to",
                              e.target.value
                            )
                          }
                          required={passenger.loai_hanh_khach === "nguoi_lon"}
                          placeholder="Nhập số giấy tờ"
                        />
                      </div>

                      <div className="form-group">
                        <label>Số ghế (tùy chọn)</label>
                        <input
                          type="text"
                          value={passenger.so_ghe}
                          onChange={(e) =>
                            handlePassengerChange(
                              index,
                              "so_ghe",
                              e.target.value
                            )
                          }
                          placeholder="Ví dụ: 12A"
                        />
                      </div>

                      <div className="form-group price-preview">
                        <label>Giá vé</label>
                        <div className="price-display">
                          {formatCurrency(calculatePassengerPrice(passenger))}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* Seat Selection Section */}
              {flight && (
                <div className="form-section">
                  <h3>Chọn ghế</h3>
                  {loadingSeats ? (
                    <div className="loading-seats">
                      <div className="loading-spinner"></div>
                      <p>Đang tải sơ đồ ghế...</p>
                    </div>
                  ) : (
                    <SeatMap
                      flightId={flight.id}
                      selectedSeats={selectedSeats}
                      onSeatSelect={handleSeatSelect}
                      bookedSeats={seatData?.ghe_da_dat || []}
                      reservedSeats={seatData?.ghe_giu_cho || []}
                      seatLayout={seatData?.so_do_ghe}
                      totalSeats={
                        seatData?.tong_so_ghe ||
                        flight.may_bay?.tong_so_ghe ||
                        180
                      }
                      fareClass={fareClass}
                      allFareClasses={seatData?.gia_ve || []}
                    />
                  )}
                </div>
              )}

              {/* Contact Info Section */}
              <div className="form-section">
                <h3>Thông tin liên hệ</h3>
                <div className="form-grid">
                  <div className="form-group">
                    <label>
                      Email <span className="required">*</span>
                    </label>
                    <input
                      type="email"
                      value={contactInfo.email}
                      onChange={(e) => {
                        handleContactChange("email", e.target.value);
                        // Clear error when user types
                        if (error && error.includes("Email")) {
                          setError(null);
                        }
                      }}
                      required
                      placeholder="email@example.com"
                      pattern="[^\s@]+@[^\s@]+\.[^\s@]+"
                    />
                  </div>

                  <div className="form-group">
                    <label>
                      Số điện thoại <span className="required">*</span>
                    </label>
                    <input
                      type="tel"
                      value={contactInfo.so_dien_thoai}
                      onChange={(e) =>
                        handleContactChange("so_dien_thoai", e.target.value)
                      }
                      required
                      placeholder="0123456789"
                    />
                  </div>

                  <div className="form-group">
                    <label>
                      Tên đầy đủ <span className="required">*</span>
                    </label>
                    <input
                      type="text"
                      value={contactInfo.ten_day_du}
                      onChange={(e) =>
                        handleContactChange("ten_day_du", e.target.value)
                      }
                      required
                      placeholder="Nhập tên đầy đủ"
                    />
                  </div>
                </div>
              </div>

              {/* Error Message */}
              {error && (
                <div className="error-message">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="8" x2="12" y2="12" />
                    <line x1="12" y1="16" x2="12.01" y2="16" />
                  </svg>
                  <span>{error}</span>
                </div>
              )}

              {/* Submit Button */}
              <div className="form-actions">
                <button
                  type="button"
                  className="btn-cancel"
                  onClick={() => navigate(-1)}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="btn-submit"
                  disabled={submitting}
                >
                  {submitting
                    ? "Đang xử lý..."
                    : `Đặt vé - ${formatCurrency(calculateTotalPrice())}`}
                </button>
              </div>
            </form>
          </div>

          {/* Price Summary Sidebar */}
          <aside className="price-summary">
            <div className="summary-card">
              <h3>Tổng tiền</h3>
              <div className="price-breakdown">
                {passengers.map((passenger, index) => (
                  <div key={index} className="price-item">
                    <div className="price-label">
                      Hành khách {index + 1} (
                      {passenger.loai_hanh_khach === "nguoi_lon"
                        ? "Người lớn"
                        : passenger.loai_hanh_khach === "tre_em"
                        ? "Trẻ em"
                        : "Em bé"}
                      ) {passenger.so_ghe && `- Ghế ${passenger.so_ghe}`}
                    </div>
                    <div className="price-value">
                      {formatCurrency(calculatePassengerPrice(passenger))}
                    </div>
                  </div>
                ))}
                <div className="price-total">
                  <div className="total-label">Tổng cộng</div>
                  <div className="total-value">
                    {formatCurrency(calculateTotalPrice())}
                  </div>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </main>

      {renderFooter()}
    </div>
  );
}
