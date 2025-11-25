import React from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import "../../styles/customerHome.css";

export default function AboutUs() {
  const navigate = useNavigate();
  const { user } = useAuth();

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
          <Link to="/about" className="nav-link active">
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

  return (
    <div className="customer-home">
      {renderHeader()}

      <main className="static-page">
        <div className="static-page-container">
          <h1 className="static-page-title">Về chúng tôi</h1>

          <section className="static-page-section">
            <h2>Giới thiệu</h2>
            <p>
              Flight Booking là nền tảng đặt vé máy bay trực tuyến hàng đầu tại
              Việt Nam, được thành lập với sứ mệnh mang đến cho khách hàng những
              trải nghiệm đặt vé nhanh chóng, tiện lợi và giá cả hợp lý nhất.
            </p>
            <p>
              Chúng tôi hợp tác với các hãng hàng không uy tín trong và ngoài
              nước để mang đến cho bạn nhiều lựa chọn chuyến bay phong phú, phù
              hợp với mọi nhu cầu và ngân sách.
            </p>
          </section>

          <section className="static-page-section">
            <h2>Tầm nhìn</h2>
            <p>
              Trở thành nền tảng đặt vé máy bay số 1 tại Việt Nam, được khách
              hàng tin tưởng và lựa chọn hàng đầu cho mọi chuyến bay.
            </p>
          </section>

          <section className="static-page-section">
            <h2>Sứ mệnh</h2>
            <ul>
              <li>Mang đến dịch vụ đặt vé nhanh chóng, tiện lợi và an toàn</li>
              <li>Cam kết giá cả minh bạch, cạnh tranh nhất thị trường</li>
              <li>Hỗ trợ khách hàng 24/7 với đội ngũ chuyên nghiệp</li>
              <li>Không ngừng cải thiện và nâng cao chất lượng dịch vụ</li>
            </ul>
          </section>

          <section className="static-page-section">
            <h2>Giá trị cốt lõi</h2>
            <div className="values-grid">
              <div className="value-item">
                <h3>Uy tín</h3>
                <p>
                  Đảm bảo tính minh bạch và đáng tin cậy trong mọi giao dịch
                </p>
              </div>
              <div className="value-item">
                <h3>Chất lượng</h3>
                <p>Luôn đặt chất lượng dịch vụ lên hàng đầu</p>
              </div>
              <div className="value-item">
                <h3>Khách hàng</h3>
                <p>Khách hàng là trung tâm của mọi hoạt động</p>
              </div>
              <div className="value-item">
                <h3>Đổi mới</h3>
                <p>Không ngừng cải tiến công nghệ và dịch vụ</p>
              </div>
            </div>
          </section>

          <section className="static-page-section">
            <h2>Thông tin liên hệ</h2>
            <div className="contact-info">
              <p>
                <strong>Email:</strong> support@flightbooking.com
              </p>
              <p>
                <strong>Hotline:</strong> 1900 1234
              </p>
              <p>
                <strong>Địa chỉ:</strong> 123 Đường ABC, Quận XYZ, TP.HCM
              </p>
              <p>
                <strong>Giờ làm việc:</strong> 24/7
              </p>
            </div>
          </section>
        </div>
      </main>

      {renderFooter()}
    </div>
  );
}
