// src/pages/ReportsPage.jsx
import React, { useState, useEffect } from 'react';
import { reportService, officeService, doctorService } from '../services/api';
import { BarChart3, Activity, Users, Building2, TrendingUp, CheckCircle } from 'lucide-react';
import './ReportsPage.css';

export default function ReportsPage() {
  const [offices, setOffices] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [officeOccupancy, setOfficeOccupancy] = useState([]);
  const [doctorProductivity, setDoctorProductivity] = useState([]);
  const [noShowPatients, setNoShowPatients] = useState([]);
  const [stats, setStats] = useState({ completed: 0, occupancy: 0, noShows: 0 });
  const [loading, setLoading] = useState(true);
  const [dateRange, setDateRange] = useState({ from: '', to: '' });

  useEffect(() => { 
    const today = new Date(); 
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1); 
    setDateRange({ 
      from: firstDay.toISOString().split('T')[0], 
      to: today.toISOString().split('T')[0] 
    }); 
  }, []);

  useEffect(() => { 
    if (dateRange.from && dateRange.to) loadReports(); 
  }, [dateRange]);

  const loadReports = async () => {
    setLoading(true);
    try {
      const [officesRes, doctorsRes, occupancyRes, productivityRes, noShowRes] = await Promise.all([
        officeService.getAll(0, 100),
        doctorService.getAll(0, 100),
        reportService.getOfficeOccupancy(dateRange.from, dateRange.to, 0, 100),
        reportService.getDoctorProductivity(dateRange.from, dateRange.to, 0, 100),
        reportService.getNoShowPatients(dateRange.from, dateRange.to, 0, 100)
      ]);
      setOffices(officesRes.data.content || officesRes.data || []);
      setDoctors(doctorsRes.data.content || doctorsRes.data || []);
      setOfficeOccupancy(occupancyRes.data.content || occupancyRes.data || []);
      setDoctorProductivity(productivityRes.data.content || productivityRes.data || []);
      setNoShowPatients(noShowRes.data.content || noShowRes.data || []);
      
      const completedTotal = (productivityRes.data.content || productivityRes.data || []).reduce((sum, d) => sum + (d.completedAppointments || 0), 0);
      const noShowTotal = (noShowRes.data.content || noShowRes.data || []).length;
      const avgOccupancy = (occupancyRes.data.content || occupancyRes.data || []).reduce((sum, o) => sum + (o.appointmentCount || 0), 0);
      
      setStats({ completed: completedTotal, occupancy: avgOccupancy, noShows: noShowTotal });
    } catch (error) { 
      console.error('Error loading reports:', error); 
    } finally { 
      setLoading(false); 
    }
  };

  const getMaxOccupancy = () => Math.max(...officeOccupancy.map(o => o.appointmentCount || 0), 1);

  return (
    <div className="reports-page">
      <div className="page-header">
        <div>
          <h1 className="page-heading">Reportes</h1>
          <p className="page-description">Estadísticas de uso y productividad del sistema</p>
        </div>
      </div>

      <div className="date-range-bar">
        <label>Rango de fechas:</label>
        <input type="date" value={dateRange.from} onChange={e => setDateRange({ ...dateRange, from: e.target.value })} />
        <span>a</span>
        <input type="date" value={dateRange.to} onChange={e => setDateRange({ ...dateRange, to: e.target.value })} />
      </div>

      <div className="stats-grid-mini">
        <div className="stat-mini-card">
          <div className="stat-mini-icon blue"><CheckCircle size={24} /></div>
          <div>
            <div className="stat-mini-value">{stats.completed}</div>
            <div className="stat-mini-label">Citas completadas</div>
          </div>
        </div>
        <div className="stat-mini-card">
          <div className="stat-mini-icon green"><TrendingUp size={24} /></div>
          <div>
            <div className="stat-mini-value">{Math.round((stats.occupancy / (offices.length * 20)) * 100)}%</div>
            <div className="stat-mini-label">Ocupación promedio</div>
          </div>
        </div>
        <div className="stat-mini-card">
          <div className="stat-mini-icon orange"><Users size={24} /></div>
          <div>
            <div className="stat-mini-value">{stats.noShows}</div>
            <div className="stat-mini-label">Inasistencias totales</div>
          </div>
        </div>
      </div>

      <div className="reports-grid">
        <div className="report-card">
          <div className="report-header">
            <h2>Ocupación de consultorios</h2>
            <Building2 size={20} />
          </div>
          <table className="report-table">
            <thead>
              <tr><th>CONSULTORIO</th><th>UBICACIÓN</th><th>SLOTS TOTALES</th><th>OCUPADOS</th><th>OCUPACIÓN</th></tr>
            </thead>
            <tbody>
              {officeOccupancy.map(o => (
                <tr key={o.officeId}>
                  <td>{o.officeName}</td>
                  <td>{offices.find(off => off.id === o.officeId)?.location || '—'}</td>
                  <td>20</td>
                  <td>{o.appointmentCount || 0}</td>
                  <td>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{ width: `${((o.appointmentCount || 0) / 20) * 100}%` }}></div>
                      <span>{Math.round(((o.appointmentCount || 0) / 20) * 100)}%</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="report-card">
          <div className="report-header">
            <h2>Productividad por doctor</h2>
            <Activity size={20} />
          </div>
          <div className="doctor-list">
            {doctorProductivity.map(d => (
              <div key={d.doctorId} className="doctor-item">
                <div>
                  <div className="doctor-name">{d.doctorName}</div>
                  <div className="doctor-specialty">{d.specialtyName}</div>
                </div>
                <div className="doctor-stats">
                  <span className="completed-count">{d.completedAppointments} citas</span>
                  <div className="mini-progress">
                    <div className="mini-progress-fill" style={{ width: `${Math.min((d.completedAppointments / stats.completed) * 100, 100)}%` }}></div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}