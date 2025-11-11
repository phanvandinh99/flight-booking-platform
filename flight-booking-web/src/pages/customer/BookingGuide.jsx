import React from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import "../../styles/customerHome.css";

export default function BookingGuide() {
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
          <Link to="/about" className="nav-link">
            Về chúng tôi
          </Link>
          <Link to="/guide" className="nav-link active">
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
          <h1 className="static-page-title">Hướng dẫn đặt vé</h1>

          <section className="static-page-section">
            <h2>Bước 1: Tìm kiếm chuyến bay</h2>
            <ol>
              <li>
                Truy cập trang chủ và điền thông tin tìm kiếm:
                <ul>
                  <li>Chọn sân bay đi và sân bay đến</li>
                  <li>
                    Chọn ngày khởi hành (và ngày về nếu là chuyến khứ hồi)
                  </li>
                  <li>Chọn số lượng hành khách (người lớn, trẻ em, em bé)</li>
                  <li>Chọn loại chuyến (một chiều hoặc khứ hồi)</li>
                </ul>
              </li>
              <li>Nhấn nút "Tìm kiếm" để xem danh sách chuyến bay phù hợp</li>
            </ol>
          </section>

          <section className="static-page-section">
            <h2>Bước 2: Chọn chuyến bay</h2>
            <ol>
              <li>
                Xem danh sách chuyến bay với thông tin chi tiết:
                <ul>
                  <li>Giờ khởi hành và hạ cánh</li>
                  <li>Thời gian bay</li>
                  <li>Hãng hàng không</li>
                  <li>Loại máy bay</li>
                  <li>Giá vé</li>
                </ul>
              </li>
              <li>Chọn hạng vé phù hợp (Phổ thông, Thương gia, Hạng nhất)</li>
              <li>Nhấn "Xem chi tiết" để xem thông tin đầy đủ</li>
              <li>Nhấn "Đặt vé" để tiếp tục</li>
            </ol>
          </section>

          <section className="static-page-section">
            <h2>Bước 3: Chọn ghế</h2>
            <ol>
              <li>Xem sơ đồ ghế máy bay</li>
              <li>
                Chọn ghế cho từng hành khách:
                <ul>
                  <li>Ghế màu trắng: Còn trống</li>
                  <li>Ghế màu đỏ: Đang được bạn chọn</li>
                  <li>Ghế màu xanh: Đang được giữ chỗ</li>
                  <li>Ghế màu xám: Đã được đặt</li>
                </ul>
              </li>
              <li>
                Lưu ý: Mỗi loại ghế (VIP, Thương gia, Phổ thông cao cấp, Phổ
                thông) có giá khác nhau
              </li>
            </ol>
          </section>

          <section className="static-page-section">
            <h2>Bước 4: Nhập thông tin hành khách</h2>
            <ol>
              <li>
                Điền thông tin cho từng hành khách:
                <ul>
                  <li>Họ và tên (bắt buộc)</li>
                  <li>Số giấy tờ tùy thân (bắt buộc cho người lớn)</li>
                  <li>Loại giấy tờ (Căn cước công dân hoặc Hộ chiếu)</li>
                  <li>Loại hành khách (Người lớn, Trẻ em, Em bé)</li>
                </ul>
              </li>
              <li>
                Điền thông tin liên hệ:
                <ul>
                  <li>Họ tên đầy đủ</li>
                  <li>Email</li>
                  <li>Số điện thoại</li>
                </ul>
              </li>
            </ol>
          </section>

          <section className="static-page-section">
            <h2>Bước 5: Thanh toán</h2>
            <ol>
              <li>Kiểm tra lại thông tin đặt vé và tổng tiền</li>
              <li>
                Chọn phương thức thanh toán:
                <ul>
                  <li>Thẻ ngân hàng</li>
                  <li>Ví điện tử</li>
                  <li>Chuyển khoản</li>
                </ul>
              </li>
              <li>Hoàn tất thanh toán</li>
              <li>Nhận email xác nhận đặt vé</li>
            </ol>
          </section>

          <section className="static-page-section">
            <h2>Lưu ý quan trọng</h2>
            <div className="notice-box">
              <h3>Thời gian giữ chỗ</h3>
              <p>
                Sau khi đặt vé, bạn có <strong>15 phút</strong> để hoàn tất
                thanh toán. Sau thời gian này, chỗ ngồi sẽ được giải phóng nếu
                chưa thanh toán.
              </p>
            </div>
            <div className="notice-box">
              <h3>Giá vé</h3>
              <ul>
                <li>Người lớn: 100% giá vé</li>
                <li>Trẻ em (2-11 tuổi): 75% giá vé</li>
                <li>Em bé (dưới 2 tuổi): 10% giá vé</li>
              </ul>
            </div>
            <div className="notice-box">
              <h3>Hủy/Đổi vé</h3>
              <p>
                Vui lòng liên hệ với chúng tôi qua hotline hoặc email để được hỗ
                trợ hủy hoặc đổi vé. Phí hủy/đổi vé tùy thuộc vào chính sách của
                từng hãng hàng không.
              </p>
            </div>
          </section>

          <section className="static-page-section">
            <h2>Câu hỏi thường gặp</h2>
            <div className="faq-item">
              <h3>Tôi có thể đặt vé cho bao nhiêu người?</h3>
              <p>Bạn có thể đặt vé cho tối đa 9 người trong một lần đặt.</p>
            </div>
            <div className="faq-item">
              <h3>Tôi cần đăng nhập để đặt vé không?</h3>
              <p>
                Có, bạn cần đăng ký và đăng nhập tài khoản để đặt vé. Đăng ký
                miễn phí và nhanh chóng.
              </p>
            </div>
            <div className="faq-item">
              <h3>Tôi có thể chọn ghế trước không?</h3>
              <p>
                Có, bạn có thể chọn ghế khi đặt vé. Mỗi loại ghế có giá khác
                nhau.
              </p>
            </div>
            <div className="faq-item">
              <h3>Làm sao để nhận vé sau khi đặt?</h3>
              <p>
                Bạn sẽ nhận email xác nhận đặt vé ngay sau khi thanh toán thành
                công. Bạn có thể sử dụng mã đặt vé để check-in tại sân bay.
              </p>
            </div>
          </section>

          <section className="static-page-section">
            <h2>Hỗ trợ</h2>
            <p>
              Nếu bạn gặp bất kỳ vấn đề nào trong quá trình đặt vé, vui lòng
              liên hệ với chúng tôi:
            </p>
            <ul>
              <li>
                <strong>Hotline:</strong> 1900 1234 (24/7)
              </li>
              <li>
                <strong>Email:</strong> support@flightbooking.com
              </li>
              <li>
                <strong>Chat trực tuyến:</strong> Có sẵn trên website
              </li>
            </ul>
          </section>
        </div>
      </main>

      {renderFooter()}
    </div>
  );
}
