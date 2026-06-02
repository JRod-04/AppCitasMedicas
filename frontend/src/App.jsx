// src/App.jsx
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import Dashboard from './pages/Dashboard';
import PatientsPage from './pages/PatientsPage';
import OfficesPage from './pages/OfficesPage';
import AppointmentsPage from './pages/AppointmentsPage'; // Tu página existente
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/patients" element={<PatientsPage />} />
          <Route path="/offices" element={<OfficesPage />} />
          <Route path="/appointments" element={<AppointmentsPage />} />
          {/* Rutas adicionales para cuando las tengas */}
          <Route path="/doctors" element={<div>Doctores - Próximamente</div>} />
          <Route path="/catalog" element={<div>Catálogo - Próximamente</div>} />
          <Route path="/availability" element={<div>Disponibilidad - Próximamente</div>} />
          <Route path="/reports" element={<div>Reportes - Próximamente</div>} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;