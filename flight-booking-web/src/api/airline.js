import client from "./client";

/**
 * API cho Airline Representative - Quản lý Máy bay
 */

// Lấy danh sách máy bay
export async function getAircrafts() {
  const res = await client.get("/airline/aircrafts");
  return res.data;
}

// Lấy chi tiết máy bay
export async function getAircraft(id) {
  const res = await client.get(`/airline/aircrafts/${id}`);
  return res.data;
}

// Tạo máy bay mới
export async function createAircraft(data) {
  const res = await client.post("/airline/aircrafts", data);
  return res.data;
}

// Cập nhật máy bay
export async function updateAircraft(id, data) {
  const res = await client.put(`/airline/aircrafts/${id}`, data);
  return res.data;
}

// Xóa máy bay
export async function deleteAircraft(id) {
  const res = await client.delete(`/airline/aircrafts/${id}`);
  return res.data;
}

/**
 * API cho Airline Representative - Quản lý Chuyến bay
 */

// Lấy danh sách chuyến bay
export async function getFlights(params = {}) {
  const res = await client.get("/airline/flights", { params });
  return res.data;
}

// Lấy chi tiết chuyến bay
export async function getFlight(id) {
  const res = await client.get(`/airline/flights/${id}`);
  return res.data;
}

// Tạo chuyến bay mới
export async function createFlight(data) {
  const res = await client.post("/airline/flights", data);
  return res.data;
}

// Cập nhật chuyến bay
export async function updateFlight(id, data) {
  const res = await client.put(`/airline/flights/${id}`, data);
  return res.data;
}

// Xóa chuyến bay
export async function deleteFlight(id) {
  const res = await client.delete(`/airline/flights/${id}`);
  return res.data;
}

// Lấy danh sách tuyến bay đã phê duyệt
export async function getApprovedRoutes() {
  const res = await client.get("/airline/flights/routes/approved");
  return res.data;
}

/**
 * API cho Airline Representative - Quản lý Giá vé
 */

// Lấy danh sách giá vé
export async function getPricing(params = {}) {
  const res = await client.get("/airline/pricing", { params });
  return res.data;
}

// Lấy chi tiết giá vé
export async function getPricingDetail(id) {
  const res = await client.get(`/airline/pricing/${id}`);
  return res.data;
}

// Tạo giá vé mới
export async function createPricing(data) {
  const res = await client.post("/airline/pricing", data);
  return res.data;
}

// Cập nhật giá vé
export async function updatePricing(id, data) {
  const res = await client.put(`/airline/pricing/${id}`, data);
  return res.data;
}

// Xóa giá vé
export async function deletePricing(id) {
  const res = await client.delete(`/airline/pricing/${id}`);
  return res.data;
}

// Lấy danh sách chuyến bay để tạo giá vé
export async function getFlightsForPricing() {
  const res = await client.get("/airline/pricing/flights");
  return res.data;
}

/**
 * API cho Airline Representative - Quản lý Đặt vé
 */

// Lấy danh sách đặt vé
export async function getBookings(params = {}) {
  const res = await client.get("/airline/bookings", { params });
  return res.data;
}

// Lấy chi tiết đặt vé
export async function getBooking(id) {
  const res = await client.get(`/airline/bookings/${id}`);
  return res.data;
}

// Cập nhật trạng thái đặt vé
export async function updateBookingStatus(id, status) {
  const res = await client.put(`/airline/bookings/${id}/status`, {
    trang_thai: status,
  });
  return res.data;
}

// Lấy thống kê đặt vé
export async function getBookingStatistics(params = {}) {
  const res = await client.get("/airline/bookings/statistics", { params });
  return res.data;
}

// Lấy danh sách chuyến bay để xem đặt vé
export async function getFlightsForBookings() {
  const res = await client.get("/airline/bookings/flights");
  return res.data;
}

/**
 * API cho Airline Representative - Báo cáo
 */

// Báo cáo doanh thu theo ngày
export async function getDailyRevenue(params = {}) {
  const res = await client.get("/airline/reports/daily-revenue", { params });
  return res.data;
}

// Báo cáo doanh thu theo tuần
export async function getWeeklyRevenue(params = {}) {
  const res = await client.get("/airline/reports/weekly-revenue", { params });
  return res.data;
}

// Báo cáo doanh thu theo tháng
export async function getMonthlyRevenue(params = {}) {
  const res = await client.get("/airline/reports/monthly-revenue", { params });
  return res.data;
}

// Báo cáo theo chuyến bay
export async function getFlightReport(params = {}) {
  const res = await client.get("/airline/reports/flight-report", { params });
  return res.data;
}

// Báo cáo theo hạng vé
export async function getFareClassReport(params = {}) {
  const res = await client.get("/airline/reports/fare-class-report", {
    params,
  });
  return res.data;
}

// Tổng quan báo cáo
export async function getOverviewReport(params = {}) {
  const res = await client.get("/airline/reports/overview", { params });
  return res.data;
}
