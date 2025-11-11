import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { loginApi } from "../api/auth";
import { useAuth } from "../auth/AuthContext";
import AuthLayout from "../components/layouts/AuthLayout";

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const data = await loginApi({ email, password });
      login(data.user, data.token);
      if (data.user.vai_tro === "admin") navigate("/admin", { replace: true });
      else if (data.user.vai_tro === "dai_dien_hang")
        navigate("/airline", { replace: true });
      else navigate("/", { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Đăng nhập" subtitle="Hệ Thống Quản Lý Chuyến Bay">
      <form onSubmit={onSubmit} className="auth-form">
        {error && (
          <div className="auth-error" role="alert">
            {error}
          </div>
        )}

        <div className="auth-form-group">
          <label htmlFor="email" className="auth-form-label">
            Email
          </label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="auth-form-input"
            placeholder="you@example.com"
            required
            disabled={loading}
            autoComplete="email"
          />
        </div>

        <div className="auth-form-group">
          <label htmlFor="password" className="auth-form-label">
            Mật khẩu
          </label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="auth-form-input"
            placeholder="Nhập mật khẩu"
            required
            disabled={loading}
            autoComplete="current-password"
          />
        </div>

        <button type="submit" className="auth-button" disabled={loading}>
          {loading ? (
            <>
              <span className="auth-loading"></span>
              Đang đăng nhập...
            </>
          ) : (
            "Đăng nhập"
          )}
        </button>

        <div className="auth-form-footer">
          <p className="auth-form-footer-text">
            Chưa có tài khoản?{" "}
            <Link to="/register" className="auth-form-footer-link">
              Đăng ký ngay
            </Link>
          </p>
        </div>
      </form>
    </AuthLayout>
  );
}
