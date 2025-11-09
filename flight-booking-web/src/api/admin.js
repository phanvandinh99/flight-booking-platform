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
