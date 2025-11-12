import React, { useState, useEffect } from "react";
import "../styles/seatMap.css";

export default function SeatMap({
  flightId,
  selectedSeats = [],
  onSeatSelect,
  bookedSeats = [],
  reservedSeats = [],
  seatLayout = null,
  totalSeats = 180,
  fareClass = null,
  allFareClasses = [],
}) {
  const [seats, setSeats] = useState([]);

  useEffect(() => {
    generateSeatMap();
  }, [
    seatLayout,
    totalSeats,
    bookedSeats,
    reservedSeats,
    selectedSeats,
    fareClass,
    allFareClasses,
  ]);

  const getFareClassLabel = (hangVe) => {
    const labels = {
      hang_nhat: "VIP",
      thuong_gia: "Thương gia",
      pho_thong_cao_cap: "Phổ thông cao cấp",
      pho_thong: "Phổ thông",
    };
    return labels[hangVe] || hangVe;
  };

  const getSeatFareClass = (row, letter) => {
    // Phân loại ghế theo vị trí:
    // - Hàng 1-3: VIP/Hạng nhất (nếu có)
    // - Hàng 4-8: Thương gia (nếu có)
    // - Hàng 9-15: Phổ thông cao cấp (nếu có)
    // - Còn lại: Phổ thông

    if (row <= 3) {
      // VIP section
      const vipClass = allFareClasses.find((f) => f.hang_ve === "hang_nhat");
      if (vipClass) return { ...vipClass, label: "VIP" };
    }

    if (row >= 4 && row <= 8) {
      // Business section
      const businessClass = allFareClasses.find(
        (f) => f.hang_ve === "thuong_gia"
      );
      if (businessClass) return { ...businessClass, label: "Thương gia" };
    }

    if (row >= 9 && row <= 15) {
      // Premium economy section
      const premiumClass = allFareClasses.find(
        (f) => f.hang_ve === "pho_thong_cao_cap"
      );
      if (premiumClass) return { ...premiumClass, label: "Phổ thông cao cấp" };
    }

    // Economy section
    const economyClass = allFareClasses.find((f) => f.hang_ve === "pho_thong");
    if (economyClass) return { ...economyClass, label: "Phổ thông" };

    // Default to selected fare class
    if (fareClass) {
      return {
        hang_ve: fareClass.hang_ve,
        gia: fareClass.gia,
        label: getFareClassLabel(fareClass.hang_ve),
      };
    }

    return { hang_ve: "pho_thong", gia: 0, label: "Phổ thông" };
  };

  const generateSeatMap = () => {
    // Nếu có sơ đồ ghế từ backend, sử dụng nó
    if (seatLayout && Array.isArray(seatLayout) && seatLayout.length > 0) {
      const processedSeats = seatLayout.map((seat) => {
        const fareClassInfo = getSeatFareClass(
          seat.row || 1,
          seat.letter || "A"
        );
        let status = "available";

        if (bookedSeats.includes(seat.number)) {
          status = "booked";
        } else if (reservedSeats.includes(seat.number)) {
          status = "reserved";
        } else if (selectedSeats.includes(seat.number)) {
          status = "selected";
        }

        return {
          ...seat,
          status,
          fareClass: fareClassInfo.hang_ve,
          price: fareClassInfo.gia,
          fareClassLabel: fareClassInfo.label,
        };
      });

      // Sắp xếp: giữ nguyên cấu trúc hàng, chỉ sắp xếp theo hàng (hàng có giá cao hơn hiển thị trước)
      // Nhưng thực tế chúng ta muốn giữ nguyên thứ tự hàng, chỉ phân biệt bằng màu sắc
      // Không cần sort lại, giữ nguyên thứ tự từ seatLayout

      setSeats(processedSeats);
      return;
    }

    // Nếu không có, tạo sơ đồ mặc định
    const rows = Math.ceil(totalSeats / 6);
    const seatLetters = ["A", "B", "C", "D", "E", "F"];
    const generatedSeats = [];

    for (let row = 1; row <= rows; row++) {
      seatLetters.forEach((letter) => {
        const seatNumber = `${row}${letter}`;
        let status = "available";

        if (bookedSeats.includes(seatNumber)) {
          status = "booked";
        } else if (reservedSeats.includes(seatNumber)) {
          status = "reserved";
        } else if (selectedSeats.includes(seatNumber)) {
          status = "selected";
        }

        const fareClassInfo = getSeatFareClass(row, letter);

        generatedSeats.push({
          number: seatNumber,
          row: row,
          letter: letter,
          status: status,
          fareClass: fareClassInfo.hang_ve,
          price: fareClassInfo.gia,
          fareClassLabel: fareClassInfo.label,
        });
      });
    }

    // Giữ nguyên thứ tự hàng (hàng 1, 2, 3...), chỉ phân biệt bằng màu sắc và giá

    setSeats(generatedSeats);
  };

  const handleSeatClick = (seat) => {
    if (seat.status === "booked" || seat.status === "reserved") {
      return;
    }

    if (onSeatSelect) {
      onSeatSelect(seat);
    }
  };

  const getSeatClass = (seat) => {
    const baseClass = "seat";
    let fareClassModifier = "";

    // Thêm class theo hạng ghế
    switch (seat.fareClass) {
      case "hang_nhat":
        fareClassModifier = " seat-vip";
        break;
      case "thuong_gia":
        fareClassModifier = " seat-business";
        break;
      case "pho_thong_cao_cap":
        fareClassModifier = " seat-premium";
        break;
      default:
        fareClassModifier = " seat-economy";
    }

    switch (seat.status) {
      case "booked":
        return `${baseClass} seat-booked${fareClassModifier}`;
      case "reserved":
        return `${baseClass} seat-reserved${fareClassModifier}`;
      case "selected":
        return `${baseClass} seat-selected${fareClassModifier}`;
      default:
        return `${baseClass} seat-available${fareClassModifier}`;
    }
  };

  const formatCurrency = (amount) => {
    if (!amount) return "N/A";
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      maximumFractionDigits: 0,
    }).format(amount);
  };

  // Nhóm ghế theo hàng (sau khi đã sắp xếp)
  const seatsByRow = {};
  seats.forEach((seat) => {
    if (!seatsByRow[seat.row]) {
      seatsByRow[seat.row] = [];
    }
    seatsByRow[seat.row].push(seat);
  });

  // Lấy danh sách các hạng ghế có sẵn để hiển thị trong legend
  const availableFareClasses = Array.from(
    new Set(seats.map((s) => s.fareClass).filter(Boolean))
  );

  // Xác định các hàng có emergency exit (thường ở giữa cabin)
  const rowNumbers = Object.keys(seatsByRow)
    .map(Number)
    .sort((a, b) => a - b);
  const totalRows = rowNumbers.length;
  const exitRows = [];
  if (totalRows > 20) {
    // Thêm exit ở 1/3 và 2/3 chiều dài cabin
    const exit1 = Math.floor(totalRows * 0.33);
    const exit2 = Math.floor(totalRows * 0.67);
    exitRows.push(rowNumbers[exit1], rowNumbers[exit2]);
  } else if (totalRows > 10) {
    // Thêm exit ở giữa
    const exit = Math.floor(totalRows * 0.5);
    exitRows.push(rowNumbers[exit]);
  }

  return (
    <div className="seat-map-container">
      <div className="seat-map-header">
        <h3>Chọn ghế</h3>
        <div className="seat-legend">
          <div className="legend-item">
            <div className="legend-seat seat-available"></div>
            <span>Trống</span>
          </div>
          <div className="legend-item">
            <div className="legend-seat seat-selected"></div>
            <span>Đang chọn</span>
          </div>
          <div className="legend-item">
            <div className="legend-seat seat-reserved"></div>
            <span>Giữ chỗ</span>
          </div>
          <div className="legend-item">
            <div className="legend-seat seat-booked"></div>
            <span>Đã đặt</span>
          </div>
        </div>
        {availableFareClasses.length > 0 && (
          <div className="fare-class-legend">
            {availableFareClasses.map((fc) => {
              const fareClassInfo =
                allFareClasses.find((f) => f.hang_ve === fc) || {};
              return (
                <div key={fc} className="fare-class-item">
                  <div className={`fare-class-badge fare-${fc}`}>
                    {getFareClassLabel(fc)}
                  </div>
                  <span className="fare-class-price">
                    {formatCurrency(fareClassInfo.gia)}
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div className="airplane-body">
        {/* Cockpit */}
        <div className="cockpit">
          <div className="cockpit-window"></div>
        </div>

        {/* Section labels - Chỉ hiển thị khi có nhiều hơn 1 loại ghế */}
        {availableFareClasses.length > 1 && (
          <div className="seat-sections">
            {availableFareClasses.includes("hang_nhat") && (
              <div className="section-label section-vip">
                <span>✨ VIP / Hạng nhất (Hàng 1-3) - Giá cao nhất</span>
              </div>
            )}
            {availableFareClasses.includes("thuong_gia") && (
              <div className="section-label section-business">
                <span>💼 Thương gia (Hàng 4-8)</span>
              </div>
            )}
            {availableFareClasses.includes("pho_thong_cao_cap") && (
              <div className="section-label section-premium">
                <span>⭐ Phổ thông cao cấp (Hàng 9-15)</span>
              </div>
            )}
            <div className="section-label section-economy">
              <span>💺 Phổ thông (Hàng còn lại) - Giá thấp nhất</span>
            </div>
          </div>
        )}

        <div className="seat-map">
          {Object.keys(seatsByRow)
            .sort((a, b) => parseInt(a) - parseInt(b))
            .map((rowNum) => {
              const row = parseInt(rowNum);
              const isExitRow = exitRows.includes(row);

              return (
                <div key={rowNum} className="seat-row-wrapper">
                  {/* Emergency Exit Indicator */}
                  {isExitRow && (
                    <div className="emergency-exit-row">
                      <div className="exit-arrow left">
                        <svg
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="#dc2626"
                          strokeWidth="2.5"
                        >
                          <path d="M5 12h14M12 5l7 7-7 7" />
                        </svg>
                      </div>
                      <div className="exit-label">LỐI THOÁT HIỂM</div>
                      <div className="exit-arrow right">
                        <svg
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="#dc2626"
                          strokeWidth="2.5"
                        >
                          <path d="M19 12H5M12 5l-7 7 7 7" />
                        </svg>
                      </div>
                    </div>
                  )}

                  <div className="seat-row">
                    <div className="row-number">{rowNum}</div>
                    <div className="seats-group">
                      {/* Left side: A, B, C */}
                      <div className="seats-left">
                        {seatsByRow[rowNum]
                          .filter((s) => ["A", "B", "C"].includes(s.letter))
                          .map((seat) => (
                            <button
                              key={seat.number}
                              className={getSeatClass(seat)}
                              onClick={() => handleSeatClick(seat)}
                              disabled={
                                seat.status === "booked" ||
                                seat.status === "reserved"
                              }
                              title={`${seat.number} - ${
                                seat.fareClassLabel
                              } - ${formatCurrency(seat.price)}`}
                            >
                              <span className="seat-letter">{seat.letter}</span>
                              {seat.price > 0 && (
                                <span
                                  className={`seat-price-badge ${
                                    seat.status === "available"
                                      ? "badge-visible"
                                      : "badge-hidden"
                                  }`}
                                >
                                  {(seat.price / 1000).toFixed(0)}k
                                </span>
                              )}
                            </button>
                          ))}
                      </div>

                      {/* Aisle */}
                      <div className="aisle"></div>

                      {/* Right side: D, E, F */}
                      <div className="seats-right">
                        {seatsByRow[rowNum]
                          .filter((s) => ["D", "E", "F"].includes(s.letter))
                          .map((seat) => (
                            <button
                              key={seat.number}
                              className={getSeatClass(seat)}
                              onClick={() => handleSeatClick(seat)}
                              disabled={
                                seat.status === "booked" ||
                                seat.status === "reserved"
                              }
                              title={`${seat.number} - ${
                                seat.fareClassLabel
                              } - ${formatCurrency(seat.price)}`}
                            >
                              <span className="seat-letter">{seat.letter}</span>
                              {seat.price > 0 && (
                                <span
                                  className={`seat-price-badge ${
                                    seat.status === "available"
                                      ? "badge-visible"
                                      : "badge-hidden"
                                  }`}
                                >
                                  {(seat.price / 1000).toFixed(0)}k
                                </span>
                              )}
                            </button>
                          ))}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
        </div>

        {/* Wings */}
        <div className="airplane-wings">
          <div className="wing wing-left"></div>
          <div className="wing wing-right"></div>
        </div>

        {/* Rear Lavatory */}
        <div className="rear-facilities">
          <div className="facility lavatory">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <circle cx="12" cy="8" r="3" />
              <path d="M12 11v6M9 17h6" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  );
}
