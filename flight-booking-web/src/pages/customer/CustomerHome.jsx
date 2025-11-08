import React from "react";

export default function CustomerHome() {
  return (
    <div
      style={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #e0e7ff 0%, #f0f9ff 100%)",
      }}
    >
      <header
        style={{
          padding: "18px 24px",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <div style={{ fontWeight: 700, fontSize: 18 }}>Flight Booking</div>
        <nav style={{ display: "flex", gap: 16 }}>
          <a href="#" style={{ color: "#1d4ed8" }}>
            Trang chủ
          </a>
          <a href="#" style={{ color: "#1d4ed8" }}>
            Tìm chuyến bay
          </a>
          <a href="/login" style={{ color: "#1d4ed8" }}>
            Đăng nhập
          </a>
        </nav>
      </header>
      <main style={{ padding: 24 }}>
        <section
          style={{
            maxWidth: 960,
            margin: "0 auto",
            background: "#fff",
            borderRadius: 16,
            padding: 24,
            boxShadow: "0 10px 30px rgba(0,0,0,0.08)",
          }}
        >
          <h1 style={{ marginTop: 0 }}>Đặt vé máy bay nhanh chóng</h1>
          <p style={{ color: "#555" }}>
            Tìm kiếm và đặt vé cho hành trình của bạn chỉ trong vài bước.
          </p>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(4, 1fr)",
              gap: 12,
              marginTop: 16,
            }}
          >
            <input
              placeholder="Điểm đi"
              style={{
                padding: "10px 12px",
                borderRadius: 8,
                border: "1px solid #ccd",
              }}
            />
            <input
              placeholder="Điểm đến"
              style={{
                padding: "10px 12px",
                borderRadius: 8,
                border: "1px solid #ccd",
              }}
            />
            <input
              placeholder="Ngày đi"
              type="date"
              style={{
                padding: "10px 12px",
                borderRadius: 8,
                border: "1px solid #ccd",
              }}
            />
            <button
              style={{
                border: "none",
                borderRadius: 8,
                background: "#1d4ed8",
                color: "#fff",
                cursor: "pointer",
              }}
            >
              Tìm chuyến bay
            </button>
          </div>
        </section>
      </main>
    </div>
  );
}
