// src/App.js
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import Dashboard from './pages/Dashboard';
import PatientsPage from './pages/PatientsPage';
import DoctorsPage from './pages/DoctorsPage';
import CatalogPage from './pages/CatalogPage';
import OfficesPage from './pages/OfficesPage';
import AvailabilityPage from './pages/AvailabilityPage';
import AppointmentsPage from './pages/AppointmentsPage';
import ReportsPage from './pages/ReportsPage';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/patients" element={<PatientsPage />} />
          <Route path="/doctors" element={<DoctorsPage />} />
          <Route path="/catalog" element={<CatalogPage />} />
          <Route path="/offices" element={<OfficesPage />} />
          <Route path="/availability" element={<AvailabilityPage />} />
          <Route path="/appointments" element={<AppointmentsPage />} />
          <Route path="/reports" element={<ReportsPage />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;