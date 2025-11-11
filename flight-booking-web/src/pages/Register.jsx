import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerApi } from "../api/auth";
import { useAuth } from "../auth/AuthContext";
import AuthLayout from "../components/layouts/AuthLayout";

export default function Register() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState({
    ten_day_du: "",
    email: "",
    so_dien_thoai: "",
    password: "",
    password_confirmation: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error when user types
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
    if (error) setError("");
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.ten_day_du.trim()) {
      newErrors.ten_day_du = "Vui lòng nhập họ tên";
    }

    if (!formData.email.trim()) {
      newErrors.email = "Vui lòng nhập email";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Email không hợp lệ";
    }

    if (!formData.so_dien_thoai.trim()) {
      newErrors.so_dien_thoai = "Vui lòng nhập số điện thoại";
    } else if (
      !/^[0-9]{10,11}$/.test(formData.so_dien_thoai.replace(/\s/g, ""))
    ) {
      newErrors.so_dien_thoai = "Số điện thoại phải có 10-11 chữ số";
    }

    if (!formData.password) {
      newErrors.password = "Vui lòng nhập mật khẩu";
    } else if (formData.password.length < 8) {
      newErrors.password = "Mật khẩu phải có ít nhất 8 ký tự";
    }

    if (!formData.password_confirmation) {
      newErrors.password_confirmation = "Vui lòng xác nhận mật khẩu";
    } else if (formData.password !== formData.password_confirmation) {
      newErrors.password_confirmation = "Mật khẩu xác nhận không khớp";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      const data = await registerApi({
        ten_day_du: formData.ten_day_du.trim(),
        email: formData.email.trim(),
        so_dien_thoai: formData.so_dien_thoai.trim(),
        password: formData.password,
        password_confirmation: formData.password_confirmation,
      });

      // Auto login after registration
      login(data.user, data.token);
      navigate("/", { replace: true });
    } catch (err) {
      if (err.response?.data?.errors) {
        // Handle validation errors from backend
        const backendErrors = err.response.data.errors;
        const formattedErrors = {};
        Object.keys(backendErrors).forEach((key) => {
          formattedErrors[key] = Array.isArray(backendErrors[key])
            ? backendErrors[key][0]
            : backendErrors[key];
        });
        setErrors(formattedErrors);
      } else {
        setError(err.message || "Đăng ký thất bại. Vui lòng thử lại.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Đăng ký tài khoản"
      subtitle="Tạo tài khoản khách hàng mới"
    >
      <form onSubmit={onSubmit} className="auth-form">
        {error && (
          <div className="auth-error" role="alert">
            {error}
          </div>
        )}

        <div className="auth-form-group">
          <label htmlFor="ten_day_du" className="auth-form-label">
            Họ và tên <span className="text-red-500">*</span>
          </label>
          <input
            id="ten_day_du"
            name="ten_day_du"
            type="text"
            value={formData.ten_day_du}
            onChange={handleChange}
            className={`auth-form-input ${
              errors.ten_day_du ? "auth-form-input-error" : ""
            }`}
            placeholder="Nhập họ và tên đầy đủ"
            required
            disabled={loading}
            autoComplete="name"
          />
          {errors.ten_day_du && (
            <span className="auth-form-error-text">{errors.ten_day_du}</span>
          )}
        </div>

        <div className="auth-form-group">
          <label htmlFor="email" className="auth-form-label">
            Email <span className="text-red-500">*</span>
          </label>
          <input
            id="email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            className={`auth-form-input ${
              errors.email ? "auth-form-input-error" : ""
            }`}
            placeholder="you@example.com"
            required
            disabled={loading}
            autoComplete="email"
          />
          {errors.email && (
            <span className="auth-form-error-text">{errors.email}</span>
          )}
        </div>

        <div className="auth-form-group">
          <label htmlFor="so_dien_thoai" className="auth-form-label">
            Số điện thoại <span className="text-red-500">*</span>
          </label>
          <input
            id="so_dien_thoai"
            name="so_dien_thoai"
            type="tel"
            value={formData.so_dien_thoai}
            onChange={handleChange}
            className={`auth-form-input ${
              errors.so_dien_thoai ? "auth-form-input-error" : ""
            }`}
            placeholder="0123456789"
            required
            disabled={loading}
            autoComplete="tel"
          />
          {errors.so_dien_thoai && (
            <span className="auth-form-error-text">{errors.so_dien_thoai}</span>
          )}
        </div>

        <div className="auth-form-group">
          <label htmlFor="password" className="auth-form-label">
            Mật khẩu <span className="text-red-500">*</span>
          </label>
          <input
            id="password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            className={`auth-form-input ${
              errors.password ? "auth-form-input-error" : ""
            }`}
            placeholder="Tối thiểu 8 ký tự"
            required
            disabled={loading}
            autoComplete="new-password"
          />
          {errors.password && (
            <span className="auth-form-error-text">{errors.password}</span>
          )}
        </div>

        <div className="auth-form-group">
          <label htmlFor="password_confirmation" className="auth-form-label">
            Xác nhận mật khẩu <span className="text-red-500">*</span>
          </label>
          <input
            id="password_confirmation"
            name="password_confirmation"
            type="password"
            value={formData.password_confirmation}
            onChange={handleChange}
            className={`auth-form-input ${
              errors.password_confirmation ? "auth-form-input-error" : ""
            }`}
            placeholder="Nhập lại mật khẩu"
            required
            disabled={loading}
            autoComplete="new-password"
          />
          {errors.password_confirmation && (
            <span className="auth-form-error-text">
              {errors.password_confirmation}
            </span>
          )}
        </div>

        <button type="submit" className="auth-button" disabled={loading}>
          {loading ? (
            <>
              <span className="auth-loading"></span>
              Đang đăng ký...
            </>
          ) : (
            "Đăng ký"
          )}
        </button>

        <div className="auth-form-footer">
          <p className="auth-form-footer-text">
            Đã có tài khoản?{" "}
            <Link to="/login" className="auth-form-footer-link">
              Đăng nhập ngay
            </Link>
          </p>
        </div>
      </form>
    </AuthLayout>
  );
}
