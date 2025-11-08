import client from "./client";

export async function loginApi({ email, password }) {
  const res = await client.post("/login", { email, password });
  return res.data; // { message, user, token }
}

export async function logoutApi() {
  const res = await client.post("/logout");
  return res.data;
}
