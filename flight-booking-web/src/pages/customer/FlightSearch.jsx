import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams, useLocation } from "react-router-dom";
import { searchFlights, getAirlines, getFlightDetail } from "../../api/customer";
import { useAuth } from "../../auth/AuthContext";
import "../../styles/flightSearch.css";

export default function FlightSearch() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchResults, setSearchResults] = useState(null);
  const [airlines, setAirlines] = useState([]);
  const [filters, setFilters] = useState({
    gia_tu: "",
    gia_den: "",
    gio_khoi_hanh_tu: "",
    gio_khoi_hanh_den: "",
    hang_hang_khong: [],
  });
  const [sortBy, setSortBy] = useState("gia_tang_dan");

  useEffect(() => {
    loadAirlines();
    const flightId = searchParams.get("flight");
    if (flightId) {
      // Redirect to dedicated flight detail page
      navigate(`/flight/${flightId}`, { replace: true });
    } else {
      performSearch();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  const loadAirlines = async () => {
    try {
      const response = await getAirlines();
      setAirlines(response.data || []);
    } catch (err) {
      console.error("Error loading airlines:", err);
    }
  };

  const loadFlightDetail = async (flightId) => {
    try {
      setLoading(true);
      setError(null);
      const response = await getFlightDetail(flightId);
      const flight = response.data;
      // Format response to match search results structure
      setSearchResults({
        loai_chuyen: "mot_chieu",
        san_bay_di: flight.tuyen_bay?.san_bay_di,
        san_bay_den: flight.tuyen_bay?.san_bay_den,
        ngay_khoi_hanh: flight.gio_khoi_hanh,
        hanh_khach: {
          tong_so: 1,
          nguoi_lon: 1,
          tre_em: 0,
          em_be: 0,
        },
        chuyen_bay_di: [flight],
        chuyen_bay_ve: null,
      });
    } catch (err) {
      setError(err.response?.data?.message || "Không thể tải chi tiết chuyến bay");
      console.error("Error loading flight detail:", err);
    } finally {
      setLoading(false);
    }
  };

  const performSearch = async () => {
    try {
      setLoading(true);
      setError(null);

      const searchData = {
        san_bay_di: searchParams.get("san_bay_di"),
        san_bay_den: searchParams.get("san_bay_den"),
        ngay_khoi_hanh: searchParams.get("ngay_khoi_hanh"),
        loai_chuyen: searchParams.get("loai_chuyen") || "mot_chieu",
        nguoi_lon: parseInt(searchParams.get("nguoi_lon")) || 1,
        tre_em: parseInt(searchParams.get("tre_em")) || 0,
        em_be: parseInt(searchParams.get("em_be")) || 0,
        hang_ve: searchParams.get("hang_ve") || "",
      };

      // Validate required fields
      if (!searchData.san_bay_di || !searchData.san_bay_den || !searchData.ngay_khoi_hanh) {
        setError("Vui lòng nhập đầy đủ thông tin tìm kiếm");
        setLoading(false);
        return;
      }

      if (searchData.loai_chuyen === "khu_hoi") {
        searchData.ngay_ve = searchParams.get("ngay_ve");
      }

      // Apply filters
      if (filters.gia_tu) searchData.gia_tu = parseFloat(filters.gia_tu);
      if (filters.gia_den) searchData.gia_den = parseFloat(filters.gia_den);
      if (filters.gio_khoi_hanh_tu)
        searchData.gio_khoi_hanh_tu = filters.gio_khoi_hanh_tu;
      if (filters.gio_khoi_hanh_den)
        searchData.gio_khoi_hanh_den = filters.gio_khoi_hanh_den;
      if (filters.hang_hang_khong.length > 0) {
        searchData.hang_hang_khong = filters.hang_hang_khong.map(Number);
      }

      const response = await searchFlights(searchData);
      setSearchResults(response.data || response);
    } catch (err) {
      setError(err.response?.data?.message || "Không thể tìm kiếm chuyến bay");
      console.error("Error searching flights:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (field, value) => {
    setFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleAirlineFilter = (airlineId, checked) => {
    setFilters((prev) => {
      const newAirlines = checked
        ? [...prev.hang_hang_khong, airlineId]
        : prev.hang_hang_khong.filter((id) => id !== airlineId);
      return {
        ...prev,
        hang_hang_khong: newAirlines,
      };
    });
  };

  const applyFilters = () => {
    performSearch();
  };

  const sortFlights = (flights) => {
    if (!flights || flights.length === 0) return flights;

    const sorted = [...flights];
    switch (sortBy) {
      case "gia_tang_dan":
        return sorted.sort((a, b) => (a.tong_gia || 0) - (b.tong_gia || 0));
      case "gia_giam_dan":
        return sorted.sort((a, b) => (b.tong_gia || 0) - (a.tong_gia || 0));
      case "gio_khoi_hanh_som":
        return sorted.sort(
          (a, b) => new Date(a.gio_khoi_hanh) - new Date(b.gio_khoi_hanh)
        );
      case "gio_khoi_hanh_muon":
        return sorted.sort(
          (a, b) => new Date(b.gio_khoi_hanh) - new Date(a.gio_khoi_hanh)
        );
      case "thoi_gian_bay_ngan":
        return sorted.sort((a, b) => {
          const timeA = new Date(a.gio_ha_canh) - new Date(a.gio_khoi_hanh);
          const timeB = new Date(b.gio_ha_canh) - new Date(b.gio_khoi_hanh);
          return timeA - timeB;
        });
      default:
        return sorted;
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
      weekday: "short",
      day: "2-digit",
      month: "2-digit",
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

  const handleSelectFlight = (flight, type = "di") => {
    // Navigate to booking page
    navigate("/booking", {
      state: {
        flight: flight,
        type: type,
        searchData: searchResults,
        selectedFlightDi: type === "di" ? flight : null,
        selectedFlightVe: type === "ve" ? flight : null,
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
      <div className="flight-search-page">
        {renderHeader()}
        <main className="search-main">
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tìm kiếm chuyến bay...</p>
          </div>
        </main>
        {renderFooter()}
      </div>
    );
  }

  if (error) {
    return (
      <div className="flight-search-page">
        {renderHeader()}
        <main className="search-main">
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
            <p>{error}</p>
            <button className="btn-retry" onClick={() => navigate("/")}>
              Tìm kiếm lại
            </button>
          </div>
        </main>
        {renderFooter()}
      </div>
    );
  }

  if (!searchResults) {
    return null;
  }

  const flightsDi = sortFlights(searchResults.chuyen_bay_di || []);
  const flightsVe = sortFlights(searchResults.chuyen_bay_ve || []);

  return (
    <div className="flight-search-page">
      {renderHeader()}

      <main className="search-main">
        <div className="search-info">
          <div className="route-info">
            <div className="route-item">
              <span className="airport-name-large">
                {searchResults.san_bay_di?.ten_san_bay || searchResults.san_bay_di?.ma_san_bay}
              </span>
              <span className="airport-code-small">
                {searchResults.san_bay_di?.ma_san_bay}
              </span>
            </div>
            <div className="route-arrow">→</div>
            <div className="route-item">
              <span className="airport-name-large">
                {searchResults.san_bay_den?.ten_san_bay || searchResults.san_bay_den?.ma_san_bay}
              </span>
              <span className="airport-code-small">
                {searchResults.san_bay_den?.ma_san_bay}
              </span>
            </div>
          </div>
          <div className="search-details">
            <span>
              {searchResults.ngay_khoi_hanh
                ? formatDate(searchResults.ngay_khoi_hanh)
                : searchResults.chuyen_bay_di?.[0]?.gio_khoi_hanh
                ? formatDate(searchResults.chuyen_bay_di[0].gio_khoi_hanh)
                : "N/A"}{" "}
              • {searchResults.hanh_khach?.tong_so || 1} hành khách •{" "}
              {searchResults.loai_chuyen === "khu_hoi"
                ? "Khứ hồi"
                : "Một chiều"}
            </span>
          </div>
        </div>

        <div className="search-content">
          {/* Filters Sidebar */}
          <aside className="filters-sidebar">
            <h3>Bộ lọc</h3>

            <div className="filter-group">
              <label>Giá vé</label>
              <div className="price-range">
                <input
                  type="number"
                  placeholder="Từ"
                  value={filters.gia_tu}
                  onChange={(e) => handleFilterChange("gia_tu", e.target.value)}
                />
                <span>-</span>
                <input
                  type="number"
                  placeholder="Đến"
                  value={filters.gia_den}
                  onChange={(e) =>
                    handleFilterChange("gia_den", e.target.value)
                  }
                />
              </div>
            </div>

            <div className="filter-group">
              <label>Giờ khởi hành</label>
              <div className="time-range">
                <input
                  type="time"
                  value={filters.gio_khoi_hanh_tu}
                  onChange={(e) =>
                    handleFilterChange("gio_khoi_hanh_tu", e.target.value)
                  }
                />
                <span>-</span>
                <input
                  type="time"
                  value={filters.gio_khoi_hanh_den}
                  onChange={(e) =>
                    handleFilterChange("gio_khoi_hanh_den", e.target.value)
                  }
                />
              </div>
            </div>

            <div className="filter-group">
              <label>Hãng hàng không</label>
              <div className="airline-filters">
                {airlines.map((airline) => (
                  <label key={airline.id} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={filters.hang_hang_khong.includes(airline.id)}
                      onChange={(e) =>
                        handleAirlineFilter(airline.id, e.target.checked)
                      }
                    />
                    <span>{airline.ten_hang}</span>
                  </label>
                ))}
              </div>
            </div>

            <button className="btn-apply-filters" onClick={applyFilters}>
              Áp dụng bộ lọc
            </button>
          </aside>

          {/* Results */}
          <div className="results-section">
            <div className="results-header">
              <h2>
                {flightsDi.length} chuyến bay đi
                {searchResults.loai_chuyen === "khu_hoi" &&
                  ` • ${flightsVe.length} chuyến bay về`}
              </h2>
              <div className="sort-controls">
                <label>Sắp xếp:</label>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                >
                  <option value="gia_tang_dan">Giá tăng dần</option>
                  <option value="gia_giam_dan">Giá giảm dần</option>
                  <option value="gio_khoi_hanh_som">Giờ khởi hành sớm</option>
                  <option value="gio_khoi_hanh_muon">Giờ khởi hành muộn</option>
                  <option value="thoi_gian_bay_ngan">Thời gian bay ngắn</option>
                </select>
              </div>
            </div>

            {/* Flights Di */}
            <div className="flights-list">
              {flightsDi.length === 0 ? (
                <div className="no-results">
                  <p>Không tìm thấy chuyến bay đi phù hợp</p>
                </div>
              ) : (
                flightsDi.map((flight) => (
                  <div key={flight.id} className="flight-card">
                    <div className="flight-info">
                      <div className="flight-time">
                        <div className="time-departure">
                          <span className="time">
                            {formatTime(flight.gio_khoi_hanh)}
                          </span>
                          <span className="airport-name">
                            {flight.tuyen_bay?.san_bay_di?.ten_san_bay || flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                          </span>
                          <span className="airport-code">
                            {flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                          </span>
                        </div>
                        <div className="duration">
                          <span>
                            {calculateDuration(
                              flight.gio_khoi_hanh,
                              flight.gio_ha_canh
                            )}
                          </span>
                        </div>
                        <div className="time-arrival">
                          <span className="time">
                            {formatTime(flight.gio_ha_canh)}
                          </span>
                          <span className="airport-name">
                            {flight.tuyen_bay?.san_bay_den?.ten_san_bay || flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                          </span>
                          <span className="airport-code">
                            {flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                          </span>
                        </div>
                      </div>
                      <div className="flight-details">
                        <div className="airline-info">
                          <span className="airline-name">
                            {flight.hang_hang_khong?.ten_hang}
                          </span>
                          <span className="flight-code">
                            {flight.ma_chuyen_bay}
                          </span>
                        </div>
                        <div className="aircraft-info">
                          {flight.may_bay?.loai_may_bay}
                        </div>
                      </div>
                    </div>
                    <div className="flight-price">
                      <div className="price">
                        {formatCurrency(flight.tong_gia || 0)}
                      </div>
                      <button
                        className="btn-select"
                        onClick={() => handleSelectFlight(flight, "di")}
                      >
                        Chọn
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Flights Ve (if round trip) */}
            {searchResults.loai_chuyen === "khu_hoi" && (
              <>
                <div className="results-header">
                  <h2>Chuyến bay về</h2>
                </div>
                <div className="flights-list">
                  {flightsVe.length === 0 ? (
                    <div className="no-results">
                      <p>Không tìm thấy chuyến bay về phù hợp</p>
                    </div>
                  ) : (
                    flightsVe.map((flight) => (
                      <div key={flight.id} className="flight-card">
                        <div className="flight-info">
                          <div className="flight-time">
                            <div className="time-departure">
                              <span className="time">
                                {formatTime(flight.gio_khoi_hanh)}
                              </span>
                              <span className="airport-name">
                                {flight.tuyen_bay?.san_bay_di?.ten_san_bay || flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                              </span>
                              <span className="airport-code">
                                {flight.tuyen_bay?.san_bay_di?.ma_san_bay}
                              </span>
                            </div>
                            <div className="duration">
                              <span>
                                {calculateDuration(
                                  flight.gio_khoi_hanh,
                                  flight.gio_ha_canh
                                )}
                              </span>
                            </div>
                            <div className="time-arrival">
                              <span className="time">
                                {formatTime(flight.gio_ha_canh)}
                              </span>
                              <span className="airport-name">
                                {flight.tuyen_bay?.san_bay_den?.ten_san_bay || flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                              </span>
                              <span className="airport-code">
                                {flight.tuyen_bay?.san_bay_den?.ma_san_bay}
                              </span>
                            </div>
                          </div>
                          <div className="flight-details">
                            <div className="airline-info">
                              <span className="airline-name">
                                {flight.hang_hang_khong?.ten_hang}
                              </span>
                              <span className="flight-code">
                                {flight.ma_chuyen_bay}
                              </span>
                            </div>
                            <div className="aircraft-info">
                              {flight.may_bay?.loai_may_bay}
                            </div>
                          </div>
                        </div>
                        <div className="flight-price">
                          <div className="price">
                            {formatCurrency(flight.tong_gia || 0)}
                          </div>
                          <button
                            className="btn-select"
                            onClick={() => handleSelectFlight(flight, "ve")}
                          >
                            Chọn
                          </button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      </main>

      {renderFooter()}
    </div>
  );
}
