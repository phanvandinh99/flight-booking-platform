import React, { useState, useEffect } from "react";
import { createPortal } from "react-dom";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { airlineMenuItems } from "../../config/airlineMenu";
import {
  getAircrafts,
  createAircraft,
  updateAircraft,
  deleteAircraft,
} from "../../api/airline";
import "../../styles/airplaneManagement.css";

export default function AirplaneManagement() {
  const [aircrafts, setAircrafts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingAircraft, setEditingAircraft] = useState(null);
  const [formData, setFormData] = useState({
    loai_may_bay: "",
    tong_so_ghe: "",
    so_do_ghe: {},
  });
  const [seatClasses, setSeatClasses] = useState([
    { id: 1, name: "pho_thong", label: "Phổ thông", rows: "", seatsPerRow: "" },
  ]);
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDirection, setSortDirection] = useState("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);

  useEffect(() => {
    loadAircrafts();
  }, []);

  const loadAircrafts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getAircrafts();
      // Normalize so_do_ghe data - đảm bảo format đúng
      const normalizedAircrafts = (response.data || []).map((aircraft) => {
        let soDoGhe = aircraft.so_do_ghe;

        // Nếu so_do_ghe là string (JSON chưa parse), parse nó
        if (typeof soDoGhe === "string") {
          try {
            soDoGhe = JSON.parse(soDoGhe);
          } catch (e) {
            soDoGhe = {};
          }
        }

        // Normalize: chỉ giữ lại các giá trị là array có phần tử, giữ lại _metadata
        const normalizedSeatMap = {};
        if (soDoGhe && typeof soDoGhe === "object" && !Array.isArray(soDoGhe)) {
          Object.keys(soDoGhe).forEach((key) => {
            const value = soDoGhe[key];
            // Giữ lại _metadata hoặc các giá trị là array có phần tử
            if (key === "_metadata" && typeof value === "object") {
              normalizedSeatMap[key] = value;
            } else if (Array.isArray(value) && value.length > 0) {
              normalizedSeatMap[key] = value;
            }
          });
        }

        // Gán lại so_do_ghe đã được normalize
        aircraft.so_do_ghe = normalizedSeatMap;

        return aircraft;
      });
      setAircrafts(normalizedAircrafts);
    } catch (err) {
      setError(
        err.response?.data?.message || "Không thể tải danh sách máy bay"
      );
      console.error("Error loading aircrafts:", err);
    } finally {
      setLoading(false);
    }
  };

  // Hàm generate mã ghế tự động
  const generateSeatCodes = (rows, seatsPerRow) => {
    const seats = [];
    const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    for (let row = 1; row <= rows; row++) {
      for (let seat = 0; seat < seatsPerRow; seat++) {
        seats.push(`${row}${letters[seat]}`);
      }
    }

    return seats;
  };

  // Parse seat configuration từ existing data
  const parseSeatConfiguration = (soDoGhe) => {
    if (!soDoGhe || typeof soDoGhe !== "object") {
      return [
        {
          id: 1,
          name: "pho_thong",
          label: "Phổ thông",
          rows: "",
          seatsPerRow: "",
        },
      ];
    }

    const classes = [];
    let id = 1;

    // Map tên loại ghế mặc định (để tương thích với dữ liệu cũ)
    const labelMap = {
      pho_thong: "Phổ thông",
      pho_thong_cao_cap: "Phổ thông cao cấp",
      thuong_gia: "Thương gia",
      hang_nhat: "Hạng nhất",
    };

    // Lấy metadata nếu có (chứa label của các loại ghế)
    const metadata = soDoGhe._metadata || {};

    for (const [key, seats] of Object.entries(soDoGhe)) {
      // Bỏ qua key _metadata
      if (key === "_metadata") continue;

      if (Array.isArray(seats) && seats.length > 0) {
        // Tính số dãy và số ghế mỗi dãy từ mã ghế
        // Giả sử mã ghế có format: số + chữ cái (ví dụ: 1A, 1B, 2A, ...)
        const rowNumbers = new Set();
        let maxSeatsInRow = 0;
        const rowSeatsMap = {};

        seats.forEach((seat) => {
          const match = seat.toString().match(/^(\d+)([A-Z])$/i);
          if (match) {
            const row = parseInt(match[1]);
            const col = match[2].toUpperCase();
            rowNumbers.add(row);

            if (!rowSeatsMap[row]) {
              rowSeatsMap[row] = new Set();
            }
            rowSeatsMap[row].add(col);
            maxSeatsInRow = Math.max(maxSeatsInRow, rowSeatsMap[row].size);
          }
        });

        if (rowNumbers.size > 0) {
          // Ưu tiên dùng label từ metadata, nếu không có thì dùng labelMap, nếu không có nữa thì format key
          let label = metadata[key] || labelMap[key];
          if (!label) {
            // Format key để hiển thị (thay underscore bằng space, capitalize)
            label = key
              .replace(/_/g, " ")
              .replace(/\b\w/g, (l) => l.toUpperCase());
          }

          classes.push({
            id: id++,
            name: key,
            label: label,
            rows: rowNumbers.size.toString(),
            seatsPerRow: maxSeatsInRow.toString(),
          });
        }
      }
    }

    // Nếu không có dữ liệu, trả về default
    if (classes.length === 0) {
      return [
        {
          id: 1,
          name: "pho_thong",
          label: "Phổ thông",
          rows: "",
          seatsPerRow: "",
        },
      ];
    }

    return classes;
  };

  const handleOpenModal = (aircraft = null) => {
    if (aircraft) {
      setEditingAircraft(aircraft);
      setFormData({
        loai_may_bay: aircraft.loai_may_bay || "",
        tong_so_ghe: aircraft.tong_so_ghe || "",
        so_do_ghe: aircraft.so_do_ghe || {},
      });
      setSeatClasses(parseSeatConfiguration(aircraft.so_do_ghe));
    } else {
      setEditingAircraft(null);
      setFormData({
        loai_may_bay: "",
        tong_so_ghe: "",
        so_do_ghe: {},
      });
      setSeatClasses([
        {
          id: 1,
          name: "pho_thong",
          label: "Phổ thông",
          rows: "",
          seatsPerRow: "",
        },
      ]);
    }
    setFormErrors({});
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingAircraft(null);
    setFormData({
      loai_may_bay: "",
      tong_so_ghe: "",
      so_do_ghe: {},
    });
    setSeatClasses([
      {
        id: 1,
        name: "pho_thong",
        label: "Phổ thông",
        rows: "",
        seatsPerRow: "",
      },
    ]);
    setFormErrors({});
  };

  // Thêm loại ghế mới
  const handleAddSeatClass = () => {
    const newId = Math.max(...seatClasses.map((sc) => sc.id), 0) + 1;
    setSeatClasses([
      ...seatClasses,
      {
        id: newId,
        name: `loai_ghe_${newId}`,
        label: "",
        rows: "",
        seatsPerRow: "",
      },
    ]);
  };

  // Xóa loại ghế
  const handleRemoveSeatClass = (id) => {
    if (seatClasses.length > 1) {
      setSeatClasses(seatClasses.filter((sc) => sc.id !== id));
    }
  };

  // Hàm chuyển đổi tiếng Việt có dấu sang không dấu
  const removeVietnameseTones = (str) => {
    if (!str) return "";
    // Chuyển đổi tất cả các ký tự có dấu sang không dấu
    str = str.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    // Chuyển đổi đ và Đ
    str = str.replace(/đ/g, "d").replace(/Đ/g, "D");
    return str;
  };

  // Cập nhật thông tin loại ghế
  const handleSeatClassChange = (id, field, value) => {
    setSeatClasses(
      seatClasses.map((sc) => {
        if (sc.id === id) {
          const updated = { ...sc, [field]: value };
          // Tự động tạo name từ label nếu label thay đổi
          if (field === "label" && value.trim()) {
            // Chuyển label thành name (lowercase, không dấu, thay khoảng trắng bằng underscore)
            const nameWithoutTones = removeVietnameseTones(value);
            updated.name = nameWithoutTones
              .toLowerCase()
              .replace(/\s+/g, "_")
              .replace(/[^a-z0-9_]/g, "");
          }
          return updated;
        }
        return sc;
      })
    );
  };

  // Tính toán và cập nhật so_do_ghe từ seatClasses
  const calculateSeatMap = () => {
    const seatMap = {};
    const seatMetadata = {}; // Lưu metadata (label) cho mỗi loại ghế
    let totalSeats = 0;

    seatClasses.forEach((seatClass) => {
      if (seatClass.rows && seatClass.seatsPerRow && seatClass.label) {
        const rows = parseInt(seatClass.rows);
        const seatsPerRow = parseInt(seatClass.seatsPerRow);

        if (rows > 0 && seatsPerRow > 0) {
          const seats = generateSeatCodes(rows, seatsPerRow);
          seatMap[seatClass.name] = seats;
          seatMetadata[seatClass.name] = seatClass.label; // Lưu label
          totalSeats += seats.length;
        }
      }
    });

    return { seatMap, totalSeats, seatMetadata };
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.loai_may_bay.trim()) {
      errors.loai_may_bay = "Loại máy bay là bắt buộc";
    }

    // Validate seat classes
    const { totalSeats } = calculateSeatMap();
    if (totalSeats === 0) {
      errors.seatClasses = "Vui lòng nhập ít nhất một loại ghế hợp lệ";
    }

    // Validate từng loại ghế
    seatClasses.forEach((seatClass) => {
      if (!seatClass.label.trim()) {
        errors[`seatClass_${seatClass.id}_label`] = "Tên loại ghế là bắt buộc";
      }

      // Kiểm tra nếu có nhập một trong hai thì cả hai đều phải có
      if (seatClass.rows || seatClass.seatsPerRow) {
        if (!seatClass.rows) {
          errors[`seatClass_${seatClass.id}_rows`] = "Vui lòng nhập số dãy";
        } else if (parseInt(seatClass.rows) < 1) {
          errors[`seatClass_${seatClass.id}_rows`] = "Số dãy phải lớn hơn 0";
        }

        if (!seatClass.seatsPerRow) {
          errors[`seatClass_${seatClass.id}_seatsPerRow`] =
            "Vui lòng nhập số ghế mỗi dãy";
        } else if (
          parseInt(seatClass.seatsPerRow) < 1 ||
          parseInt(seatClass.seatsPerRow) > 26
        ) {
          errors[`seatClass_${seatClass.id}_seatsPerRow`] =
            "Số ghế mỗi dãy phải từ 1 đến 26";
        }
      }
    });

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    // Tính toán so_do_ghe từ seatClasses
    const { seatMap, totalSeats, seatMetadata } = calculateSeatMap();

    try {
      setSubmitting(true);

      // Tạo so_do_ghe với metadata (lưu label trong một key đặc biệt)
      const soDoGheWithMetadata = {
        ...seatMap,
        _metadata: seatMetadata, // Lưu metadata để hiển thị lại
      };

      const submitData = {
        loai_may_bay: formData.loai_may_bay,
        tong_so_ghe: totalSeats,
        so_do_ghe: soDoGheWithMetadata,
      };
      if (editingAircraft) {
        await updateAircraft(editingAircraft.id, submitData);
      } else {
        await createAircraft(submitData);
      }
      await loadAircrafts();
      handleCloseModal();
    } catch (err) {
      if (err.response?.data?.errors) {
        setFormErrors(err.response.data.errors);
      } else {
        alert(err.response?.data?.message || "Có lỗi xảy ra");
      }
      console.error("Error saving aircraft:", err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa máy bay này?")) {
      return;
    }

    try {
      await deleteAircraft(id);
      await loadAircrafts();
    } catch (err) {
      alert(err.response?.data?.message || "Không thể xóa máy bay");
      console.error("Error deleting aircraft:", err);
    }
  };

  const filteredAircrafts = aircrafts.filter((aircraft) => {
    const search = searchTerm.toLowerCase();
    return (
      aircraft.loai_may_bay?.toLowerCase().includes(search) ||
      aircraft.tong_so_ghe?.toString().includes(search) ||
      aircraft.hang_hang_khong?.ten_hang?.toLowerCase().includes(search)
    );
  });

  // Sort aircrafts
  const sortedAircrafts = [...filteredAircrafts].sort((a, b) => {
    if (!sortField) return 0;

    let aValue, bValue;
    if (sortField === "hang_hang_khong") {
      aValue = a.hang_hang_khong?.ten_hang?.toLowerCase() || "";
      bValue = b.hang_hang_khong?.ten_hang?.toLowerCase() || "";
    } else {
      aValue = a[sortField]?.toString().toLowerCase() || "";
      bValue = b[sortField]?.toString().toLowerCase() || "";
    }

    if (sortDirection === "asc") {
      return aValue.localeCompare(bValue);
    } else {
      return bValue.localeCompare(aValue);
    }
  });

  // Pagination
  const totalPages = Math.ceil(sortedAircrafts.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedAircrafts = sortedAircrafts.slice(startIndex, endIndex);

  // Reset to page 1 when search or sort changes
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, sortField, sortDirection]);

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDirection("asc");
    }
  };

  const getSortIcon = (field) => {
    if (sortField !== field) {
      return (
        <svg
          width="12"
          height="12"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          opacity="0.3"
        >
          <path d="M7 13l5 5 5-5M7 6l5-5 5 5" />
        </svg>
      );
    }
    return sortDirection === "asc" ? (
      <svg
        width="12"
        height="12"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      >
        <path d="M7 13l5 5 5-5M7 6l5-5 5 5" />
      </svg>
    ) : (
      <svg
        width="12"
        height="12"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      >
        <path d="M7 6l5 5 5-5M7 13l5 5 5-5" />
      </svg>
    );
  };

  return (
    <DashboardLayout menuItems={airlineMenuItems} title="Quản lý Máy Bay">
      <div className="airplane-management-page">
        <div className="page-header">
          <div className="header-content">
            {/* <h2>Quản lý Máy Bay</h2> */}
            <p>Thêm, sửa, xóa thông tin máy bay trong hệ thống</p>
          </div>
          <div className="header-actions">
            <div className="search-box">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
              <input
                type="text"
                placeholder="Tìm kiếm máy bay..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <button className="btn-primary" onClick={() => handleOpenModal()}>
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              Thêm máy bay
            </button>
          </div>
        </div>

        {loading && aircrafts.length === 0 ? (
          <div className="loading-container">
            <div className="loading-spinner-large"></div>
            <p>Đang tải danh sách máy bay...</p>
          </div>
        ) : error ? (
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
            <h3>Không thể tải dữ liệu</h3>
            <p>{error}</p>
            <button className="btn-retry" onClick={loadAircrafts}>
              Thử lại
            </button>
          </div>
        ) : filteredAircrafts.length === 0 ? (
          <div className="empty-container">
            <div className="empty-icon">
              <svg
                width="64"
                height="64"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" />
              </svg>
            </div>
            <h3>
              {searchTerm ? "Không tìm thấy máy bay" : "Không có máy bay nào"}
            </h3>
            <p>
              {searchTerm
                ? "Thử tìm kiếm với từ khóa khác"
                : "Bắt đầu bằng cách thêm máy bay mới"}
            </p>
            {!searchTerm && (
              <button className="btn-primary" onClick={() => handleOpenModal()}>
                Thêm máy bay đầu tiên
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="aircrafts-table-container">
              <table className="aircrafts-table">
                <thead>
                  <tr>
                    <th
                      className="sortable"
                      onClick={() => handleSort("loai_may_bay")}
                    >
                      <div className="th-content">
                        Loại máy bay
                        {getSortIcon("loai_may_bay")}
                      </div>
                    </th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("tong_so_ghe")}
                    >
                      <div className="th-content">
                        Tổng số ghế
                        {getSortIcon("tong_so_ghe")}
                      </div>
                    </th>
                    <th>Sơ đồ ghế</th>
                    <th
                      className="sortable"
                      onClick={() => handleSort("hang_hang_khong")}
                    >
                      <div className="th-content">
                        Hãng hàng không
                        {getSortIcon("hang_hang_khong")}
                      </div>
                    </th>
                    <th className="th-actions">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedAircrafts.map((aircraft) => (
                    <tr key={aircraft.id}>
                      <td>
                        <span className="aircraft-type">
                          {aircraft.loai_may_bay}
                        </span>
                      </td>
                      <td>{aircraft.tong_so_ghe}</td>
                      <td>
                        <div className="seat-info">
                          {(() => {
                            // Map tên loại ghế
                            const labelMap = {
                              pho_thong: "Phổ thông",
                              pho_thong_cao_cap: "Phổ thông cao cấp",
                              thuong_gia: "Thương gia",
                              hang_nhat: "Hạng nhất",
                            };

                            // Kiểm tra so_do_ghe
                            let soDoGhe = aircraft.so_do_ghe;

                            // Nếu không có so_do_ghe hoặc không phải object
                            if (
                              !soDoGhe ||
                              typeof soDoGhe !== "object" ||
                              Array.isArray(soDoGhe)
                            ) {
                              return (
                                <span className="seat-badge">
                                  Chưa có dữ liệu
                                </span>
                              );
                            }

                            // Lấy metadata nếu có (chứa label của các loại ghế)
                            const metadata = soDoGhe._metadata || {};

                            // Lọc các entries hợp lệ (chỉ lấy array có phần tử, bỏ qua _metadata)
                            const validEntries = Object.entries(soDoGhe)
                              .filter(([key, value]) => {
                                // Bỏ qua key _metadata và chỉ lấy giá trị là array có phần tử
                                return (
                                  key !== "_metadata" &&
                                  Array.isArray(value) &&
                                  value.length > 0
                                );
                              })
                              .map(([key, seats]) => {
                                // Ưu tiên dùng label từ metadata, nếu không có thì dùng labelMap, nếu không có nữa thì format key
                                let label = metadata[key] || labelMap[key];

                                // Nếu vẫn không có, format key để hiển thị (thay underscore bằng space, capitalize)
                                if (!label) {
                                  label = key
                                    .replace(/_/g, " ")
                                    .replace(/\b\w/g, (l) => l.toUpperCase());
                                }

                                return {
                                  key,
                                  count: seats.length,
                                  label: label,
                                };
                              });

                            // Nếu không có entries hợp lệ, hiển thị "Chưa có dữ liệu"
                            if (validEntries.length === 0) {
                              return (
                                <span className="seat-badge">
                                  Chưa có dữ liệu
                                </span>
                              );
                            }

                            // Hiển thị các loại ghế
                            return validEntries.map((entry, index) => (
                              <span
                                key={entry.key || index}
                                className="seat-badge"
                              >
                                {entry.label}: {entry.count}
                              </span>
                            ));
                          })()}
                        </div>
                      </td>
                      <td>{aircraft.hang_hang_khong?.ten_hang || "N/A"}</td>
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn-edit"
                            onClick={() => handleOpenModal(aircraft)}
                            title="Sửa"
                          >
                            <svg
                              width="11"
                              height="11"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2.5"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            >
                              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                            </svg>
                          </button>
                          <button
                            className="btn-delete"
                            onClick={() => handleDelete(aircraft.id)}
                            title="Xóa"
                          >
                            <svg
                              width="11"
                              height="11"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2.5"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            >
                              <polyline points="3 6 5 6 21 6" />
                              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                              <line x1="10" y1="11" x2="10" y2="17" />
                              <line x1="14" y1="11" x2="14" y2="17" />
                            </svg>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="pagination">
                <div className="pagination-info">
                  Hiển thị {startIndex + 1}-
                  {Math.min(endIndex, sortedAircrafts.length)} trong tổng số{" "}
                  {sortedAircrafts.length} máy bay
                </div>
                <div className="pagination-controls">
                  <button
                    className="pagination-btn"
                    onClick={() => setCurrentPage(1)}
                    disabled={currentPage === 1}
                  >
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <polyline points="11 17 6 12 11 7" />
                      <polyline points="18 17 13 12 18 7" />
                    </svg>
                  </button>
                  <button
                    className="pagination-btn"
                    onClick={() => setCurrentPage(currentPage - 1)}
                    disabled={currentPage === 1}
                  >
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <polyline points="15 18 9 12 15 6" />
                    </svg>
                  </button>

                  {Array.from({ length: totalPages }, (_, i) => i + 1).map(
                    (page) => {
                      if (
                        page === 1 ||
                        page === totalPages ||
                        (page >= currentPage - 2 && page <= currentPage + 2)
                      ) {
                        return (
                          <button
                            key={page}
                            className={`pagination-btn ${
                              currentPage === page ? "active" : ""
                            }`}
                            onClick={() => setCurrentPage(page)}
                          >
                            {page}
                          </button>
                        );
                      } else if (
                        page === currentPage - 3 ||
                        page === currentPage + 3
                      ) {
                        return (
                          <span key={page} className="pagination-ellipsis">
                            ...
                          </span>
                        );
                      }
                      return null;
                    }
                  )}

                  <button
                    className="pagination-btn"
                    onClick={() => setCurrentPage(currentPage + 1)}
                    disabled={currentPage === totalPages}
                  >
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <polyline points="9 18 15 12 9 6" />
                    </svg>
                  </button>
                  <button
                    className="pagination-btn"
                    onClick={() => setCurrentPage(totalPages)}
                    disabled={currentPage === totalPages}
                  >
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <polyline points="13 17 18 12 13 7" />
                      <polyline points="6 17 11 12 6 7" />
                    </svg>
                  </button>
                </div>
              </div>
            )}
          </>
        )}

        {/* Modal Form - Rendered using Portal */}
        {showModal &&
          createPortal(
            <div className="modal-overlay" onClick={handleCloseModal}>
              <div
                className="modal-content"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="modal-header">
                  <h3>
                    {editingAircraft ? "Sửa máy bay" : "Thêm máy bay mới"}
                  </h3>
                  <button className="btn-close" onClick={handleCloseModal}>
                    <svg
                      width="24"
                      height="24"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                  </button>
                </div>

                <form onSubmit={handleSubmit} className="modal-form">
                  <div className="form-group">
                    <label htmlFor="loai_may_bay">
                      Loại máy bay <span className="required">*</span>
                    </label>
                    <input
                      id="loai_may_bay"
                      type="text"
                      value={formData.loai_may_bay}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          loai_may_bay: e.target.value,
                        })
                      }
                      className={formErrors.loai_may_bay ? "error" : ""}
                      placeholder="VD: Boeing 787-9, Airbus A321"
                      disabled={submitting}
                    />
                    {formErrors.loai_may_bay && (
                      <span className="error-message">
                        {formErrors.loai_may_bay}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label>
                      Tổng số ghế
                      {(() => {
                        const { totalSeats } = calculateSeatMap();
                        return totalSeats > 0 ? (
                          <span className="total-seats-info">
                            {" "}
                            (Tự động: {totalSeats})
                          </span>
                        ) : null;
                      })()}
                    </label>
                    <input
                      type="number"
                      min="1"
                      value={(() => {
                        const { totalSeats } = calculateSeatMap();
                        return totalSeats > 0
                          ? totalSeats
                          : formData.tong_so_ghe;
                      })()}
                      className={formErrors.tong_so_ghe ? "error" : ""}
                      placeholder="Sẽ tự động tính từ các loại ghế"
                      disabled={true}
                      readOnly
                    />
                    {formErrors.tong_so_ghe && (
                      <span className="error-message">
                        {formErrors.tong_so_ghe}
                      </span>
                    )}
                    <small className="form-hint">
                      Tổng số ghế sẽ được tính tự động từ các loại ghế bên dưới
                    </small>
                  </div>

                  {/* Dynamic Seat Classes */}
                  <div className="form-group">
                    <div className="seat-classes-header">
                      <label>
                        Sơ đồ ghế <span className="required">*</span>
                      </label>
                      <button
                        type="button"
                        className="btn-add-seat-class"
                        onClick={handleAddSeatClass}
                        disabled={submitting}
                      >
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                        >
                          <line x1="12" y1="5" x2="12" y2="19" />
                          <line x1="5" y1="12" x2="19" y2="12" />
                        </svg>
                        Thêm loại ghế
                      </button>
                    </div>
                    {formErrors.seatClasses && (
                      <span className="error-message">
                        {formErrors.seatClasses}
                      </span>
                    )}

                    {seatClasses.map((seatClass, index) => {
                      const seatClassTotal =
                        seatClass.rows && seatClass.seatsPerRow
                          ? parseInt(seatClass.rows) *
                            parseInt(seatClass.seatsPerRow)
                          : 0;

                      return (
                        <div key={seatClass.id} className="seat-class-item">
                          <div className="seat-class-header">
                            <h4>Loại ghế {index + 1}</h4>
                            {seatClasses.length > 1 && (
                              <button
                                type="button"
                                className="btn-remove-seat-class"
                                onClick={() =>
                                  handleRemoveSeatClass(seatClass.id)
                                }
                                disabled={submitting}
                                title="Xóa loại ghế"
                              >
                                <svg
                                  width="16"
                                  height="16"
                                  viewBox="0 0 24 24"
                                  fill="none"
                                  stroke="currentColor"
                                  strokeWidth="2"
                                >
                                  <line x1="18" y1="6" x2="6" y2="18" />
                                  <line x1="6" y1="6" x2="18" y2="18" />
                                </svg>
                              </button>
                            )}
                          </div>

                          <div className="form-row">
                            <div className="form-group">
                              <label>
                                Tên loại ghế <span className="required">*</span>
                              </label>
                              <input
                                type="text"
                                value={seatClass.label}
                                onChange={(e) =>
                                  handleSeatClassChange(
                                    seatClass.id,
                                    "label",
                                    e.target.value
                                  )
                                }
                                className={
                                  formErrors[`seatClass_${seatClass.id}_label`]
                                    ? "error"
                                    : ""
                                }
                                placeholder="VD: Phổ thông, Thương gia"
                                disabled={submitting}
                              />
                              {formErrors[
                                `seatClass_${seatClass.id}_label`
                              ] && (
                                <span className="error-message">
                                  {
                                    formErrors[
                                      `seatClass_${seatClass.id}_label`
                                    ]
                                  }
                                </span>
                              )}
                            </div>

                            <div className="form-group">
                              <label>
                                Số dãy <span className="required">*</span>
                              </label>
                              <input
                                type="number"
                                min="1"
                                value={seatClass.rows}
                                onChange={(e) =>
                                  handleSeatClassChange(
                                    seatClass.id,
                                    "rows",
                                    e.target.value
                                  )
                                }
                                className={
                                  formErrors[`seatClass_${seatClass.id}_rows`]
                                    ? "error"
                                    : ""
                                }
                                placeholder="VD: 20"
                                disabled={submitting}
                              />
                              {formErrors[`seatClass_${seatClass.id}_rows`] && (
                                <span className="error-message">
                                  {formErrors[`seatClass_${seatClass.id}_rows`]}
                                </span>
                              )}
                            </div>

                            <div className="form-group">
                              <label>
                                Số ghế mỗi dãy{" "}
                                <span className="required">*</span>
                              </label>
                              <input
                                type="number"
                                min="1"
                                max="26"
                                value={seatClass.seatsPerRow}
                                onChange={(e) =>
                                  handleSeatClassChange(
                                    seatClass.id,
                                    "seatsPerRow",
                                    e.target.value
                                  )
                                }
                                className={
                                  formErrors[
                                    `seatClass_${seatClass.id}_seatsPerRow`
                                  ]
                                    ? "error"
                                    : ""
                                }
                                placeholder="VD: 6 (A-F)"
                                disabled={submitting}
                              />
                              {formErrors[
                                `seatClass_${seatClass.id}_seatsPerRow`
                              ] && (
                                <span className="error-message">
                                  {
                                    formErrors[
                                      `seatClass_${seatClass.id}_seatsPerRow`
                                    ]
                                  }
                                </span>
                              )}
                              <small className="form-hint">
                                Tối đa 26 ghế (A-Z)
                              </small>
                            </div>
                          </div>

                          {seatClassTotal > 0 && (
                            <div className="seat-class-preview">
                              <small>
                                Tổng: {seatClassTotal} ghế (từ{" "}
                                {
                                  generateSeatCodes(
                                    parseInt(seatClass.rows),
                                    parseInt(seatClass.seatsPerRow)
                                  )[0]
                                }{" "}
                                đến{" "}
                                {
                                  generateSeatCodes(
                                    parseInt(seatClass.rows),
                                    parseInt(seatClass.seatsPerRow)
                                  )[seatClassTotal - 1]
                                }
                                )
                              </small>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                  <div className="modal-footer">
                    <button
                      type="button"
                      className="btn-cancel"
                      onClick={handleCloseModal}
                      disabled={submitting}
                    >
                      Hủy
                    </button>
                    <button
                      type="submit"
                      className="btn-submit"
                      disabled={submitting}
                    >
                      {submitting ? (
                        <>
                          <span className="loading-spinner"></span>
                          Đang lưu...
                        </>
                      ) : editingAircraft ? (
                        "Cập nhật"
                      ) : (
                        "Tạo mới"
                      )}
                    </button>
                  </div>
                </form>
              </div>
            </div>,
            document.body
          )}
      </div>
    </DashboardLayout>
  );
}
