import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AirlineApproval from "./pages/admin/AirlineApproval";
import AirportManagement from "./pages/admin/AirportManagement";
import RouteManagement from "./pages/admin/RouteManagement";
import Reports from "./pages/admin/Reports";
import SystemConfig from "./pages/admin/SystemConfig";
import AirlineDashboard from "./pages/airline/AirlineDashboard";
import AirplaneManagement from "./pages/airline/AirplaneManagement";
import FlightManagement from "./pages/airline/FlightManagement";
import PricingManagement from "./pages/airline/PricingManagement";
import BookingManagement from "./pages/airline/BookingManagement";
import AirlineReports from "./pages/airline/AirlineReports";
import CustomerHome from "./pages/customer/CustomerHome";
import FlightSearch from "./pages/customer/FlightSearch";
import FlightDetail from "./pages/customer/FlightDetail";
import FlightList from "./pages/customer/FlightList";
import ProtectedRoute from "./routes/ProtectedRoute";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        {/* Public customer pages */}
        <Route path="/" element={<CustomerHome />} />
        <Route path="/flights" element={<FlightList />} />
        <Route path="/search" element={<FlightSearch />} />
        <Route path="/flight/:id" element={<FlightDetail />} />

        {/* Admin protected */}
        <Route element={<ProtectedRoute allowRoles={["admin"]} />}>
          <Route path="/admin" element={<AdminDashboard />} />
          <Route
            path="/admin/airlines/approval"
            element={<AirlineApproval />}
          />
          <Route path="/admin/airports" element={<AirportManagement />} />
          <Route path="/admin/routes" element={<RouteManagement />} />
          <Route path="/admin/reports" element={<Reports />} />
          <Route path="/admin/config" element={<SystemConfig />} />
        </Route>

        {/* Airline representative protected */}
        <Route element={<ProtectedRoute allowRoles={["dai_dien_hang"]} />}>
          <Route path="/airline" element={<AirlineDashboard />} />
          <Route path="/airline/airplanes" element={<AirplaneManagement />} />
          <Route path="/airline/flights" element={<FlightManagement />} />
          <Route path="/airline/pricing" element={<PricingManagement />} />
          <Route path="/airline/bookings" element={<BookingManagement />} />
          <Route path="/airline/reports" element={<AirlineReports />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
