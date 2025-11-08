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
    const message =
      error?.response?.data?.message || error.message || "Request error";
    return Promise.reject(new Error(message));
  }
);

export default client;
