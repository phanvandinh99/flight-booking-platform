import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getAirports, getTodayFlights } from "../../api/customer";
import { useAuth } from "../../auth/AuthContext";
import "../../styles/customerHome.css";

export default function CustomerHome() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [airports, setAirports] = useState([]);
  const [todayFlights, setTodayFlights] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingFlights, setLoadingFlights] = useState(true);
  const [currentBanner, setCurrentBanner] = useState(0);
  const [searchData, setSearchData] = useState({
    san_bay_di: "",
    san_bay_den: "",
    ngay_khoi_hanh: "",
    ngay_ve: "",
    loai_chuyen: "mot_chieu",
    nguoi_lon: 1,
    tre_em: 0,
    em_be: 0,
    hang_ve: "",
  });
  const [errors, setErrors] = useState({});

  const banners = [
    {
      id: 1,
      image: "/images/banner1.jpg",
      title: "Khám phá thế giới",
      subtitle: "Đặt vé ngay hôm nay",
    },
    {
      id: 2,
      image: "/images/banner2.jpg",
      title: "Giá vé tốt nhất",
      subtitle: "So sánh và tiết kiệm",
    },
    {
      id: 3,
      image: "/images/banner3.jpg",
      title: "Bay an toàn",
      subtitle: "Dịch vụ hàng đầu",
    },
    {
      id: 4,
      image: "/images/banner4.jpg",
      title: "Trải nghiệm tuyệt vời",
      subtitle: "Hành trình đáng nhớ",
    },
  ];

  useEffect(() => {
    loadAirports();
    loadTodayFlights();

    // Auto rotate banner
    const bannerInterval = setInterval(() => {
      setCurrentBanner((prev) => (prev + 1) % banners.length);
    }, 5000);

    return () => clearInterval(bannerInterval);
  }, []);

  const loadAirports = async () => {
    try {
      const response = await getAirports();
      setAirports(response.data || []);
    } catch (err) {
      console.error("Error loading airports:", err);
    }
  };

  const loadTodayFlights = async () => {
    try {
      setLoadingFlights(true);
      const response = await getTodayFlights();
      setTodayFlights(response.data || []);
    } catch (err) {
      console.error("Error loading today flights:", err);
    } finally {
      setLoadingFlights(false);
    }
  };

  const handleInputChange = (field, value) => {
    setSearchData((prev) => ({
      ...prev,
      [field]: value,
    }));
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!searchData.san_bay_di) {
      newErrors.san_bay_di = "Vui lòng chọn sân bay đi";
    }
    if (!searchData.san_bay_den) {
      newErrors.san_bay_den = "Vui lòng chọn sân bay đến";
    }
    if (
      searchData.san_bay_di === searchData.san_bay_den &&
      searchData.san_bay_di
    ) {
      newErrors.san_bay_den = "Sân bay đến phải khác sân bay đi";
    }
    if (!searchData.ngay_khoi_hanh) {
      newErrors.ngay_khoi_hanh = "Vui lòng chọn ngày khởi hành";
    }
    if (searchData.loai_chuyen === "khu_hoi" && !searchData.ngay_ve) {
      newErrors.ngay_ve = "Vui lòng chọn ngày về";
    }
    if (searchData.ngay_khoi_hanh && searchData.ngay_ve) {
      const ngayDi = new Date(searchData.ngay_khoi_hanh);
      const ngayVe = new Date(searchData.ngay_ve);
      if (ngayVe < ngayDi) {
        newErrors.ngay_ve = "Ngày về phải sau ngày đi";
      }
    }
    if (searchData.nguoi_lon < 1 || searchData.nguoi_lon > 9) {
      newErrors.nguoi_lon = "Số người lớn phải từ 1 đến 9";
    }
    const tongHanhKhach =
      searchData.nguoi_lon + searchData.tre_em + searchData.em_be;
    if (tongHanhKhach > 9) {
      newErrors.tong_hanh_khach =
        "Tổng số hành khách không được vượt quá 9 người";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    const params = new URLSearchParams();
    params.append("san_bay_di", searchData.san_bay_di);
    params.append("san_bay_den", searchData.san_bay_den);
    params.append("ngay_khoi_hanh", searchData.ngay_khoi_hanh);
    params.append("loai_chuyen", searchData.loai_chuyen);
    params.append("nguoi_lon", searchData.nguoi_lon);
    if (searchData.tre_em > 0) params.append("tre_em", searchData.tre_em);
    if (searchData.em_be > 0) params.append("em_be", searchData.em_be);
    if (searchData.hang_ve) params.append("hang_ve", searchData.hang_ve);
    if (searchData.loai_chuyen === "khu_hoi" && searchData.ngay_ve) {
      params.append("ngay_ve", searchData.ngay_ve);
    }

    navigate(`/search?${params.toString()}`, {
      state: { searchData },
    });
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

  const handleSelectBanner = (index) => {
    setCurrentBanner(index);
  };

  return (
    <div className="customer-home">
      {/* Header */}
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
            <a href="/" className="nav-link active">
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

      <main className="customer-main">
        {/* Banner Carousel */}
        <section className="banner-section">
          <div className="banner-carousel">
            {banners.map((banner, index) => (
              <div
                key={banner.id}
                className={`banner-slide ${
                  index === currentBanner ? "active" : ""
                }`}
                style={{
                  backgroundImage: `url(${banner.image})`,
                }}
              >
                <div className="banner-overlay"></div>
                <div className="banner-content">
                  <h2>{banner.title}</h2>
                  <p>{banner.subtitle}</p>
                </div>
              </div>
            ))}
            <div className="banner-controls">
              {banners.map((_, index) => (
                <button
                  key={index}
                  className={`banner-dot ${
                    index === currentBanner ? "active" : ""
                  }`}
                  onClick={() => handleSelectBanner(index)}
                />
              ))}
            </div>
            <button
              className="banner-nav prev"
              onClick={() =>
                setCurrentBanner(
                  (prev) => (prev - 1 + banners.length) % banners.length
                )
              }
            >
              ‹
            </button>
            <button
              className="banner-nav next"
              onClick={() =>
                setCurrentBanner((prev) => (prev + 1) % banners.length)
              }
            >
              ›
            </button>
          </div>
        </section>

        {/* Search Form */}
        <div className="hero-section">
          <div className="search-form-container">
            <form onSubmit={handleSearch} className="search-form">
              <div className="trip-type-selector">
                <button
                  type="button"
                  className={`trip-type-btn ${
                    searchData.loai_chuyen === "mot_chieu" ? "active" : ""
                  }`}
                  onClick={() => {
                    handleInputChange("loai_chuyen", "mot_chieu");
                    handleInputChange("ngay_ve", "");
                  }}
                >
                  Một chiều
                </button>
                <button
                  type="button"
                  className={`trip-type-btn ${
                    searchData.loai_chuyen === "khu_hoi" ? "active" : ""
                  }`}
                  onClick={() => handleInputChange("loai_chuyen", "khu_hoi")}
                >
                  Khứ hồi
                </button>
              </div>

              <div className="search-fields">
                <div className="field-group">
                  <label>Sân bay đi</label>
                  <select
                    value={searchData.san_bay_di}
                    onChange={(e) =>
                      handleInputChange("san_bay_di", e.target.value)
                    }
                    className={errors.san_bay_di ? "error" : ""}
                  >
                    <option value="">Chọn sân bay đi</option>
                    {airports.map((airport) => (
                      <option key={airport.id} value={airport.ma_san_bay}>
                        {airport.ma_san_bay} - {airport.ten_san_bay}
                      </option>
                    ))}
                  </select>
                  {errors.san_bay_di && (
                    <span className="field-error">{errors.san_bay_di}</span>
                  )}
                </div>

                <div className="field-group">
                  <label>Sân bay đến</label>
                  <select
                    value={searchData.san_bay_den}
                    onChange={(e) =>
                      handleInputChange("san_bay_den", e.target.value)
                    }
                    className={errors.san_bay_den ? "error" : ""}
                  >
                    <option value="">Chọn sân bay đến</option>
                    {airports
                      .filter(
                        (airport) =>
                          airport.ma_san_bay !== searchData.san_bay_di
                      )
                      .map((airport) => (
                        <option key={airport.id} value={airport.ma_san_bay}>
                          {airport.ma_san_bay} - {airport.ten_san_bay}
                        </option>
                      ))}
                  </select>
                  {errors.san_bay_den && (
                    <span className="field-error">{errors.san_bay_den}</span>
                  )}
                </div>

                <div className="field-group">
                  <label>Ngày đi</label>
                  <input
                    type="date"
                    value={searchData.ngay_khoi_hanh}
                    onChange={(e) =>
                      handleInputChange("ngay_khoi_hanh", e.target.value)
                    }
                    min={new Date().toISOString().split("T")[0]}
                    className={errors.ngay_khoi_hanh ? "error" : ""}
                  />
                  {errors.ngay_khoi_hanh && (
                    <span className="field-error">{errors.ngay_khoi_hanh}</span>
                  )}
                </div>

                {searchData.loai_chuyen === "khu_hoi" && (
                  <div className="field-group">
                    <label>Ngày về</label>
                    <input
                      type="date"
                      value={searchData.ngay_ve}
                      onChange={(e) =>
                        handleInputChange("ngay_ve", e.target.value)
                      }
                      min={
                        searchData.ngay_khoi_hanh ||
                        new Date().toISOString().split("T")[0]
                      }
                      className={errors.ngay_ve ? "error" : ""}
                    />
                    {errors.ngay_ve && (
                      <span className="field-error">{errors.ngay_ve}</span>
                    )}
                  </div>
                )}

                <div className="field-group passengers">
                  <label>Hành khách</label>
                  <div className="passengers-input">
                    <div className="passenger-count">
                      <span>
                        {searchData.nguoi_lon +
                          searchData.tre_em +
                          searchData.em_be}{" "}
                        người
                      </span>
                      <button
                        type="button"
                        className="passenger-toggle"
                        onClick={(e) => {
                          e.preventDefault();
                        }}
                      >
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                        >
                          <polyline points="6 9 12 15 18 9" />
                        </svg>
                      </button>
                    </div>
                    <div className="passenger-selector">
                      <div className="passenger-item">
                        <label>Người lớn (12+)</label>
                        <div className="passenger-controls">
                          <button
                            type="button"
                            onClick={() =>
                              handleInputChange(
                                "nguoi_lon",
                                Math.max(1, searchData.nguoi_lon - 1)
                              )
                            }
                          >
                            -
                          </button>
                          <span>{searchData.nguoi_lon}</span>
                          <button
                            type="button"
                            onClick={() =>
                              handleInputChange(
                                "nguoi_lon",
                                Math.min(9, searchData.nguoi_lon + 1)
                              )
                            }
                          >
                            +
                          </button>
                        </div>
                      </div>
                      <div className="passenger-item">
                        <label>Trẻ em (2-11)</label>
                        <div className="passenger-controls">
                          <button
                            type="button"
                            onClick={() =>
                              handleInputChange(
                                "tre_em",
                                Math.max(0, searchData.tre_em - 1)
                              )
                            }
                          >
                            -
                          </button>
                          <span>{searchData.tre_em}</span>
                          <button
                            type="button"
                            onClick={() =>
                              handleInputChange(
                                "tre_em",
                                Math.min(9, searchData.tre_em + 1)
                              )
                            }
                          >
                            +
                          </button>
                        </div>
                      </div>
                      <div className="passenger-item">
                        <label>Em bé (&lt;2)</label>
                        <div className="passenger-controls">
                          <button
                            type="button"
                            onClick={() =>
                              handleInputChange(
                                "em_be",
                                Math.max(0, searchData.em_be - 1)
                              )
                            }
                          >
                            -
                          </button>
                          <span>{searchData.em_be}</span>
                          <button
                            type="button"
                            onClick={() =>
                              handleInputChange(
                                "em_be",
                                Math.min(9, searchData.em_be + 1)
                              )
                            }
                          >
                            +
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  {errors.tong_hanh_khach && (
                    <span className="field-error">
                      {errors.tong_hanh_khach}
                    </span>
                  )}
                </div>

                <div className="field-group">
                  <label>Hạng vé</label>
                  <select
                    value={searchData.hang_ve}
                    onChange={(e) =>
                      handleInputChange("hang_ve", e.target.value)
                    }
                  >
                    <option value="">Tất cả</option>
                    <option value="pho_thong">Phổ thông</option>
                    <option value="thuong_gia">Thương gia</option>
                    <option value="hang_nhat">Hạng nhất</option>
                  </select>
                </div>
              </div>

              <button type="submit" className="search-btn" disabled={loading}>
                {loading ? "Đang tìm..." : "Tìm chuyến bay"}
              </button>
            </form>
          </div>
        </div>

        {/* Today's Flights */}
        <section className="today-flights-section">
          <div className="section-container">
            <h2 className="section-title">Chuyến bay sắp tới</h2>
            {loadingFlights ? (
              <div className="loading-flights">
                <div className="loading-spinner"></div>
                <p>Đang tải chuyến bay...</p>
              </div>
            ) : todayFlights.length === 0 ? (
              <div className="no-flights">
                <p>Không có chuyến bay nào trong ngày hôm nay</p>
              </div>
            ) : (
              <div className="flights-grid">
                {todayFlights.map((flight) => (
                  <div key={flight.id} className="flight-card-mini">
                    <div className="flight-route">
                      <div className="route-item">
                        <span className="airport-name">
                          {flight.tuyen_bay?.san_bay_di?.ten_san_bay ||
                            flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                        </span>
                        <span className="time">
                          {formatTime(flight.gio_khoi_hanh)}
                        </span>
                      </div>
                      <div className="route-arrow">→</div>
                      <div className="route-item">
                        <span className="airport-name">
                          {flight.tuyen_bay?.san_bay_den?.ten_san_bay ||
                            flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                        </span>
                        <span className="time">
                          {formatTime(flight.gio_ha_canh)}
                        </span>
                      </div>
                    </div>
                    <div className="flight-info-mini">
                      <span className="airline">
                        {flight.hang_hang_khong?.ten_hang}
                      </span>
                      <span className="flight-code">
                        {flight.ma_chuyen_bay}
                      </span>
                    </div>
                    <div className="flight-price-mini">
                      {flight.gia_ve && flight.gia_ve.length > 0 ? (
                        <span className="price">
                          {formatCurrency(flight.gia_ve[0].gia)}
                        </span>
                      ) : (
                        <span className="price">Liên hệ</span>
                      )}
                      <button
                        className="btn-view-icon"
                        onClick={() => navigate(`/search?flight=${flight.id}`)}
                        title="Xem chi tiết"
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
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        {/* Features Section */}
        <div className="features-section">
          <div className="feature-card">
            <div className="feature-icon">
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
            </div>
            <h3>So sánh giá vé</h3>
            <p>Xem giá vé từ nhiều hãng hàng không cùng một lúc</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              <svg
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              </svg>
            </div>
            <h3>Đặt vé an toàn</h3>
            <p>Thanh toán bảo mật với nhiều phương thức thanh toán</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              <svg
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <h3>Giữ chỗ tạm thời</h3>
            <p>Giữ chỗ 15 phút để bạn có thời gian thanh toán</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              <svg
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
            </div>
            <h3>Quản lý đặt vé</h3>
            <p>Xem, hủy hoặc đổi vé dễ dàng trong tài khoản của bạn</p>
          </div>
        </div>
      </main>

      {/* Footer */}
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
                  <path d="M12 0C8.74 0 8.333.015 7.053.072 5.775.132 4.905.333 4.14.63c-.789.306-1.459.717-2.126 1.384S.935 3.35.63 4.14C.333 4.905.131 5.775.072 7.053.012 8.333 0 8.74 0 12s.015 3.667.072 4.947c.06 1.277.261 2.148.558 2.913.306.788.717 1.459 1.384 2.126.667.666 1.336 1.079 2.126 1.384.766.296 1.636.499 2.913.558C8.333 23.988 8.74 24 12 24s3.667-.015 4.947-.072c1.277-.06 2.148-.262 2.913-.558.788-.306 1.459-.718 2.126-1.384.666-.667 1.079-1.335 1.384-2.126.296-.765.499-1.636.558-2.913.06-1.28.072-1.687.072-4.947s-.015-3.667-.072-4.947c-.06-1.277-.262-2.149-.558-2.913-.306-.789-.718-1.459-1.384-2.126C21.319 1.347 20.651.935 19.86.63c-.765-.297-1.636-.499-2.913-.558C15.667.012 15.26 0 12 0zm0 2.16c3.203 0 3.585.016 4.85.071 1.17.055 1.805.249 2.227.415.562.217.96.477 1.382.896.419.42.679.819.896 1.381.164.422.36 1.057.413 2.227.057 1.266.07 1.646.07 4.85s-.015 3.585-.074 4.85c-.061 1.17-.256 1.805-.421 2.227-.224.562-.479.96-.899 1.382-.419.419-.824.679-1.38.896-.42.164-1.065.36-2.235.413-1.274.057-1.649.07-4.859.07-3.211 0-3.586-.015-4.859-.074-1.171-.061-1.816-.256-2.236-.421-.569-.224-.96-.479-1.379-.899-.421-.419-.69-.824-.9-1.38-.165-.42-.359-1.065-.42-2.235-.057-1.275-.07-1.65-.07-4.859 0-3.21.015-3.586.074-4.859.061-1.17.255-1.814.42-2.234.21-.57.479-.96.9-1.381.419-.419.81-.689 1.379-.898.42-.166 1.051-.361 2.221-.421 1.275-.057 1.65-.07 4.859-.07zm0 3.678c-3.405 0-6.162 2.76-6.162 6.162 0 3.405 2.76 6.162 6.162 6.162 3.405 0 6.162-2.76 6.162-6.162 0-3.405-2.76-6.162-6.162-6.162zM12 16c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4zm7.846-10.405c0 .795-.646 1.44-1.44 1.44-.795 0-1.44-.646-1.44-1.44 0-.794.646-1.439 1.44-1.439.793-.001 1.44.645 1.44 1.439z" />
                </svg>
              </a>
            </div>
          </div>
          <div className="footer-section">
            <h4>Về chúng tôi</h4>
            <ul>
              <li>
                <a href="#about">Giới thiệu</a>
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
            <h4>Trợ giúp</h4>
            <ul>
              <li>
                <a href="#faq">Câu hỏi thường gặp</a>
              </li>
              <li>
                <a href="#support">Hỗ trợ khách hàng</a>
              </li>
              <li>
                <a href="#terms">Điều khoản sử dụng</a>
              </li>
              <li>
                <a href="#privacy">Chính sách bảo mật</a>
              </li>
            </ul>
          </div>
          <div className="footer-section">
            <h4>Hướng dẫn</h4>
            <ul>
              <li>
                <a href="#guide">Hướng dẫn đặt vé</a>
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
    </div>
  );
}
