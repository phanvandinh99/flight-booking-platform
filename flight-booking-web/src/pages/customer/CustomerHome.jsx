import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getAirports } from "../../api/customer";
import { useAuth } from "../../auth/AuthContext";
import "../../styles/customerHome.css";

export default function CustomerHome() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [airports, setAirports] = useState([]);
  const [loading, setLoading] = useState(false);
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

  useEffect(() => {
    loadAirports();
  }, []);

  const loadAirports = async () => {
    try {
      const response = await getAirports();
      setAirports(response.data || []);
    } catch (err) {
      console.error("Error loading airports:", err);
    }
  };

  const handleInputChange = (field, value) => {
    setSearchData((prev) => ({
      ...prev,
      [field]: value,
    }));
    // Clear error when user types
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const handleSwapAirports = () => {
    setSearchData((prev) => ({
      ...prev,
      san_bay_di: prev.san_bay_den,
      san_bay_den: prev.san_bay_di,
    }));
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

    // Build query params
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

    // Navigate to search results with data
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

  return (
    <div className="customer-home">
      <header className="customer-header">
        <div className="header-content">
          <div className="logo">
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
        <div className="hero-section">
          <div className="hero-content">
            <h1>Tìm kiếm và đặt vé máy bay</h1>
            <p>
              So sánh giá vé từ nhiều hãng hàng không, đặt vé nhanh chóng và dễ
              dàng
            </p>
          </div>

          <div className="search-form-container">
            <form onSubmit={handleSearch} className="search-form">
              {/* Trip Type */}
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

              {/* Search Fields */}
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

                <button
                  type="button"
                  className="swap-btn"
                  onClick={handleSwapAirports}
                  title="Đổi chỗ"
                >
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <polyline points="16 3 21 3 21 8" />
                    <line x1="4" y1="20" x2="21" y2="3" />
                    <polyline points="21 16 21 21 16 21" />
                    <line x1="15" y1="15" x2="21" y2="21" />
                    <line x1="3" y1="4" x2="9" y2="10" />
                  </svg>
                </button>

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
                          // Toggle passenger selector
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
    </div>
  );
}
