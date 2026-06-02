import React, { useState, useEffect } from 'react';
import './Header.css';

export default function Header() {
  const [currentDate, setCurrentDate] = useState('');

  useEffect(() => {
    const updateDate = () => {
      const now = new Date();
      const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
      setCurrentDate(now.toLocaleDateString('es-ES', options));
    };
    updateDate();
  }, []);

  return (
    <header className="header">
      <div className="header-left">
        <h1 className="page-title">Dashboard</h1>
        <p className="current-date">{currentDate}</p>
      </div>
      <div className="header-right">
        <div className="user-info">
          <div className="user-avatar">AD</div>
          <div className="user-details">
            <span className="user-name">Admin User</span>
            <span className="user-role">Administrador</span>
          </div>
        </div>
      </div>
    </header>
  );
}