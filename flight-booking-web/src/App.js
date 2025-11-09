import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AirlineApproval from "./pages/admin/AirlineApproval";
import AirportManagement from "./pages/admin/AirportManagement";
import RouteManagement from "./pages/admin/RouteManagement";
import AirlineDashboard from "./pages/airline/AirlineDashboard";
import CustomerHome from "./pages/customer/CustomerHome";
import ProtectedRoute from "./routes/ProtectedRoute";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        {/* Public customer pages */}
        <Route path="/" element={<CustomerHome />} />

        {/* Admin protected */}
        <Route element={<ProtectedRoute allowRoles={["admin"]} />}>
          <Route path="/admin" element={<AdminDashboard />} />
          <Route
            path="/admin/airlines/approval"
            element={<AirlineApproval />}
          />
          <Route path="/admin/airports" element={<AirportManagement />} />
          <Route path="/admin/routes" element={<RouteManagement />} />
        </Route>

        {/* Airline representative protected */}
        <Route element={<ProtectedRoute allowRoles={["dai_dien_hang"]} />}>
          <Route path="/airline" element={<AirlineDashboard />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
