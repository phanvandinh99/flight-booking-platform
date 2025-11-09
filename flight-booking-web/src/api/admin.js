import client from "./client";

/**
 * API cho Admin - Phê duyệt hãng hàng không
 */

// Lấy danh sách hãng chờ duyệt
export async function getPendingAirlines() {
  const res = await client.get("/admin/airlines/pending");
  return res.data;
}

// Phê duyệt hãng
export async function approveAirline(id) {
  const res = await client.post(`/admin/airlines/${id}/approve`);
  return res.data;
}

// Từ chối hãng
export async function rejectAirline(id) {
  const res = await client.post(`/admin/airlines/${id}/reject`);
  return res.data;
}

// Kích hoạt hãng
export async function activateAirline(id) {
  const res = await client.post(`/admin/airlines/${id}/activate`);
  return res.data;
}

// Đình chỉ hãng
export async function suspendAirline(id) {
  const res = await client.post(`/admin/airlines/${id}/suspend`);
  return res.data;
}

/**
 * API cho Admin - Quản lý sân bay
 */

// Lấy danh sách sân bay
export async function getAirports() {
  const res = await client.get("/admin/airports");
  return res.data;
}

// Lấy chi tiết sân bay
export async function getAirport(id) {
  const res = await client.get(`/admin/airports/${id}`);
  return res.data;
}

// Tạo sân bay mới
export async function createAirport(data) {
  const res = await client.post("/admin/airports", data);
  return res.data;
}

// Cập nhật sân bay
export async function updateAirport(id, data) {
  const res = await client.put(`/admin/airports/${id}`, data);
  return res.data;
}

// Xóa sân bay
export async function deleteAirport(id) {
  const res = await client.delete(`/admin/airports/${id}`);
  return res.data;
}

/**
 * API cho Admin - Quản lý tuyến bay
 */

// Lấy danh sách tuyến bay
export async function getRoutes() {
  const res = await client.get("/admin/routes");
  return res.data;
}

// Lấy chi tiết tuyến bay
export async function getRoute(id) {
  const res = await client.get(`/admin/routes/${id}`);
  return res.data;
}

// Tạo tuyến bay mới
export async function createRoute(data) {
  const res = await client.post("/admin/routes", data);
  return res.data;
}

// Cập nhật tuyến bay
export async function updateRoute(id, data) {
  const res = await client.put(`/admin/routes/${id}`, data);
  return res.data;
}

// Xóa tuyến bay
export async function deleteRoute(id) {
  const res = await client.delete(`/admin/routes/${id}`);
  return res.data;
}

// Phê duyệt tuyến bay
export async function approveRoute(id) {
  const res = await client.post(`/admin/routes/${id}/approve`);
  return res.data;
}

// Thu hồi phê duyệt tuyến bay
export async function revokeRoute(id) {
  const res = await client.post(`/admin/routes/${id}/revoke`);
  return res.data;
}

/**
 * API cho Admin - Báo cáo tổng hợp
 */

// Tổng doanh thu
export async function getRevenueSummary() {
  const res = await client.get("/admin/reports/revenue/summary");
  return res.data;
}

// Doanh thu theo tháng
export async function getMonthlyRevenue(params = {}) {
  const res = await client.get("/admin/reports/revenue/monthly", { params });
  return res.data;
}

// Top hãng hàng không theo doanh thu
export async function getTopAirlines(limit = 10) {
  const res = await client.get("/admin/reports/top-airlines", {
    params: { limit },
  });
  return res.data;
}
