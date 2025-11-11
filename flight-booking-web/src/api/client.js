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
    // Giữ nguyên error object để có thể truy cập error.response
    return Promise.reject(error);
  }
);

export default client;
