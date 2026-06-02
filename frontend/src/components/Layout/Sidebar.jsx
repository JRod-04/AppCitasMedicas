import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Stethoscope,
  Building2,
  Calendar,
  Clock,
  FileText,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import './Sidebar.css';

const menuItems = [
  { path: '/', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/patients', label: 'Pacientes', icon: Users },
  { path: '/doctors', label: 'Doctores', icon: Stethoscope },
  { path: '/catalog', label: 'Catálogo', icon: FileText },
  { path: '/offices', label: 'Consultorios', icon: Building2 },
  { path: '/availability', label: 'Disponibilidad', icon: Clock },
  { path: '/appointments', label: 'Citas', icon: Calendar },
  { path: '/reports', label: 'Reportes', icon: FileText },
];

export default function Sidebar({ collapsed, setCollapsed }) {
  return (
    <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-header">
        <div className="logo">
          {!collapsed && <span className="logo-text">Sistema Médico</span>}
          {collapsed && <span className="logo-icon">🏥</span>}
        </div>
        <button className="collapse-btn" onClick={() => setCollapsed(!collapsed)}>
          {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
        </button>
      </div>

      <nav className="sidebar-nav">
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
          >
            <item.icon size={20} />
            {!collapsed && <span>{item.label}</span>}
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        {!collapsed && <span className="version">v1.0.0</span>}
      </div>
    </aside>
  );
}