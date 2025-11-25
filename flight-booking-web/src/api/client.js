import axios from "axios";

const baseURL = process.env.REACT_APP_API_BASE_URL || "/api";

const client = axios.create({
  baseURL,
  headers: { Accept: "application/json" },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem("fb_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    // Xử lý lỗi 401 Unauthorized
    if (error.response?.status === 401) {
      // Xóa token và user khỏi localStorage
      localStorage.removeItem("fb_token");
      localStorage.removeItem("fb_user");
      // Redirect đến trang login nếu đang ở trang protected
      if (
        window.location.pathname.startsWith("/airline") ||
        window.location.pathname.startsWith("/admin")
      ) {
        window.location.href = "/login";
      }
    }
    // Giữ nguyên error object để có thể truy cập error.response
    return Promise.reject(error);
  }
);

export default client;
