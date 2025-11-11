import client from "./client";

export async function registerApi({
  ten_day_du,
  email,
  so_dien_thoai,
  password,
  password_confirmation,
}) {
  const response = await client.post("/register", {
    ten_day_du,
    email,
    so_dien_thoai,
    password,
    password_confirmation,
  });
  return response.data;
}

export async function loginApi({ email, password }) {
  const res = await client.post("/login", { email, password });
  return res.data; // { message, user, token }
}

export async function logoutApi() {
  const res = await client.post("/logout");
  return res.data;
}
