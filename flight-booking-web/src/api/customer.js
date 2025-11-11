import client from "./client";

/**
 * API cho Customer - Tìm kiếm chuyến bay
 */

// Tìm kiếm chuyến bay (public endpoint)
export async function searchFlights(data) {
  const res = await client.post("/search/flights", data);
  return res.data;
}

// Lấy danh sách sân bay (public endpoint)
export async function getAirports() {
  const res = await client.get("/search/airports");
  return res.data;
}

// Lấy danh sách hãng hàng không (public endpoint)
export async function getAirlines() {
  const res = await client.get("/search/airlines");
  return res.data;
}

// Lấy chi tiết chuyến bay (public endpoint)
export async function getFlightDetail(id) {
  const res = await client.get(`/search/flights/${id}`);
  return res.data;
}

// Lấy danh sách chuyến bay hôm nay (public endpoint)
export async function getTodayFlights() {
  const res = await client.get("/search/flights/today");
  return res.data;
}

// Lấy danh sách chuyến bay với filter, sort, pagination (public endpoint)
export async function getFlightList(params = {}) {
  const res = await client.get("/search/flights/list", { params });
  return res.data;
}

/**
 * API cho Customer - Đặt vé
 */

// Đặt vé
export async function createBooking(data) {
  const res = await client.post("/customer/bookings", data);
  return res.data;
}

// Lấy danh sách đặt vé
export async function getBookings(params = {}) {
  const res = await client.get("/customer/bookings", { params });
  return res.data;
}

// Lấy chi tiết đặt vé
export async function getBooking(id) {
  const res = await client.get(`/customer/bookings/${id}`);
  return res.data;
}

// Thanh toán đặt vé
export async function payBooking(id, data) {
  const res = await client.post(`/customer/bookings/${id}/payment`, data);
  return res.data;
}

// Hủy đặt vé
export async function cancelBooking(id) {
  const res = await client.put(`/customer/bookings/${id}/cancel`);
  return res.data;
}
