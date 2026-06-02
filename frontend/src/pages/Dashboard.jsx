import React, { useState, useEffect } from 'react';
import { appointmentService, patientService, doctorService, officeService } from '../services/api';
import { Users, Calendar, CheckCircle, Clock, ArrowRight } from 'lucide-react';
import './Dashboard.css';

export default function Dashboard() {
  const [stats, setStats] = useState({
    activePatients: 0,
    totalPatients: 0,
    todayAppointments: 0,
    confirmedToday: 0,
    pendingToday: 0
  });
  const [todayAppointments, setTodayAppointments] = useState([]);
  const [recentHistory, setRecentHistory] = useState([]);
  const [weeklyData, setWeeklyData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const [patientsRes, appointmentsRes] = await Promise.all([
        patientService.getAll(0, 100),
        appointmentService.getAll(0, 100)
      ]);

      const patients = patientsRes.data.content || patientsRes.data || [];
      const appointments = appointmentsRes.data.content || appointmentsRes.data || [];

      // Estadísticas de pacientes
      const activePatients = patients.filter(p => p.status === 'ACTIVE').length;
      
      // Citas de hoy
      const today = new Date().toISOString().split('T')[0];
      const todayApps = appointments.filter(app => app.startAt?.startsWith(today));
      const confirmedToday = todayApps.filter(app => app.status === 'CONFIRMED').length;
      const pendingToday = todayApps.filter(app => app.status === 'SCHEDULED').length;

      setStats({
        activePatients,
        totalPatients: patients.length,
        todayAppointments: todayApps.length,
        confirmedToday,
        pendingToday
      });

      // Citas de hoy (para tabla)
      setTodayAppointments(todayApps.slice(0, 5));

      // Historial reciente (últimas 5)
      setRecentHistory(appointments.slice(0, 5));

      // Datos semanales (simulados - luego con reportService)
      setWeeklyData([
        { day: 'Lun', count: 9 },
        { day: 'Mar', count: 7 },
        { day: 'Mié', count: 10 },
        { day: 'Jue', count: 11 },
        { day: 'Vie', count: 5 },
        { day: 'Sáb', count: 2 }
      ]);

    } catch (error) {
      console.error('Error loading dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  const statusConfig = {
    SCHEDULED: { label: 'Programada', class: 'status-scheduled' },
    CONFIRMED: { label: 'Confirmada', class: 'status-confirmed' },
    COMPLETED: { label: 'Completada', class: 'status-completed' },
    CANCELLED: { label: 'Cancelada', class: 'status-cancelled' },
    NO_SHOW: { label: 'No Asistió', class: 'status-no-show' }
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short' });
  };

  return (
    <div className="dashboard">
      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon blue">
            <Users size={24} />
          </div>
          <div className="stat-info">
            <h3>Pacientes activos</h3>
            <p className="stat-value">{stats.activePatients}</p>
            <p className="stat-sub">de {stats.totalPatients} registrados</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon green">
            <Calendar size={24} />
          </div>
          <div className="stat-info">
            <h3>Citas del día</h3>
            <p className="stat-value">{stats.todayAppointments}</p>
            <p className="stat-sub">programadas para hoy</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon emerald">
            <CheckCircle size={24} />
          </div>
          <div className="stat-info">
            <h3>Confirmadas</h3>
            <p className="stat-value">{stats.confirmedToday}</p>
            <p className="stat-sub">listas para atención</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon orange">
            <Clock size={24} />
          </div>
          <div className="stat-info">
            <h3>Pendientes</h3>
            <p className="stat-value">{stats.pendingToday}</p>
            <p className="stat-sub">aguardando confirmación</p>
          </div>
        </div>
      </div>

      <div className="dashboard-two-columns">
        {/* Today's Appointments */}
        <div className="dashboard-card">
          <div className="card-header">
            <h2>Citas de hoy</h2>
            <span className="card-badge">{stats.todayAppointments} citas programadas</span>
          </div>
          <div className="table-responsive">
            <table className="simple-table">
              <thead>
                <tr>
                  <th>PACIENTE</th>
                  <th>DOCTOR</th>
                  <th>HORA</th>
                </tr>
              </thead>
              <tbody>
                {todayAppointments.length === 0 ? (
                  <tr><td colSpan="3" className="empty-state">No hay citas para hoy</td></tr>
                ) : (
                  todayAppointments.map(app => (
                    <tr key={app.id}>
                      <td>{app.patientName || `Paciente #${app.patientId}`}</td>
                      <td>{app.doctorName || `Dr. #${app.doctorId}`}</td>
                      <td>{formatTime(app.startAt)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          <div className="card-footer">
            <button className="link-btn">Ver todas <ArrowRight size={16} /></button>
          </div>
        </div>

        {/* Weekly Occupation */}
        <div className="dashboard-card">
          <div className="card-header">
            <h2>Ocupación semanal</h2>
            <span className="card-badge">Citas atendidas por día</span>
          </div>
          <div className="weekly-chart">
            {weeklyData.map((item) => (
              <div key={item.day} className="chart-bar-container">
                <div className="chart-label">{item.day}</div>
                <div className="chart-bar-wrapper">
                  <div 
                    className="chart-bar" 
                    style={{ width: `${(item.count / 11) * 100}%`, height: '32px' }}
                  >
                    <span className="chart-value">{item.count}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
          <div className="stats-mini">
            <div className="stat-mini-item">
              <span className="stat-mini-label">Consultorios</span>
              <span className="stat-mini-value">3</span>
            </div>
            <div className="stat-mini-item">
              <span className="stat-mini-label">Doctores</span>
              <span className="stat-mini-value">4</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent History */}
      <div className="dashboard-card full-width">
        <div className="card-header">
          <h2>Historial reciente</h2>
          <span className="card-badge">Últimas 5 citas en el sistema</span>
        </div>
        <div className="table-responsive">
          <table className="simple-table">
            <thead>
              <tr>
                <th>PACIENTE</th>
                <th>DOCTOR</th>
                <th>FECHA</th>
                <th>HORA</th>
                <th>ESTADO</th>
              </tr>
            </thead>
            <tbody>
              {recentHistory.map(app => (
                <tr key={app.id}>
                  <td>{app.patientName || `Paciente #${app.patientId}`}</td>
                  <td>{app.doctorName || `Dr. #${app.doctorId}`}</td>
                  <td>{formatDate(app.startAt)}</td>
                  <td>{formatTime(app.startAt)}</td>
                  <td>
                    <span className={`status-badge ${statusConfig[app.status]?.class || 'status-scheduled'}`}>
                      {statusConfig[app.status]?.label || app.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}