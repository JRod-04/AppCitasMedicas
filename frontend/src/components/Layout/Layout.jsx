import React, { useState } from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import './Layout.css';

export default function Layout({ children }) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  return (
    <div className="layout">
      <Sidebar collapsed={sidebarCollapsed} setCollapsed={setSidebarCollapsed} />
      <div className={`layout-main ${sidebarCollapsed ? 'expanded' : ''}`}>
        <Header />
        <main className="layout-content">{children}</main>
      </div>
    </div>
  );
}