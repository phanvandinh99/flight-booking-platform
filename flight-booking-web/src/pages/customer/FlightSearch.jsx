import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams, useLocation } from "react-router-dom";
import { searchFlights, getAirlines } from "../../api/customer";
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
    performSearch();
  }, [searchParams]);

  const loadAirlines = async () => {
    try {
      const response = await getAirlines();
      setAirlines(response.data || []);
    } catch (err) {
      console.error("Error loading airlines:", err);
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

  if (loading) {
    return (
      <div className="flight-search-page">
        <div className="loading-container">
          <div className="loading-spinner-large"></div>
          <p>Đang tìm kiếm chuyến bay...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flight-search-page">
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
      <header className="search-header">
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
            {user ? (
              <>
                <a href="/bookings" className="nav-link">
                  Đặt vé của tôi
                </a>
                <span className="user-info">{user.ten || user.email}</span>
              </>
            ) : (
              <a href="/login" className="nav-link">
                Đăng nhập
              </a>
            )}
          </nav>
        </div>
      </header>

      <main className="search-main">
        <div className="search-info">
          <div className="route-info">
            <div className="route-item">
              <span className="airport-code">
                {searchResults.san_bay_di?.ma_san_bay}
              </span>
              <span className="airport-name">
                {searchResults.san_bay_di?.ten_san_bay}
              </span>
            </div>
            <div className="route-arrow">→</div>
            <div className="route-item">
              <span className="airport-code">
                {searchResults.san_bay_den?.ma_san_bay}
              </span>
              <span className="airport-name">
                {searchResults.san_bay_den?.ten_san_bay}
              </span>
            </div>
          </div>
          <div className="search-details">
            <span>
              {formatDate(searchResults.ngay_khoi_hanh)} •{" "}
              {searchResults.hanh_khach?.tong_so || 1} hành khách •{" "}
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
                          <span className="airport">
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
                          <span className="airport">
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
                              <span className="airport">
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
                              <span className="airport">
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
    </div>
  );
}
