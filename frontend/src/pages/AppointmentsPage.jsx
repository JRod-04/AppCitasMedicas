import React, { useState, useEffect } from 'react';
import { appointmentService, doctorService, patientService, officeService, appointmentTypeService } from '../services/api';
import './AppointmentsPage.css';  // ← IMPORTAR CSS

const statusConfig = {
  SCHEDULED: { label: 'Agendada', class: 'badge-scheduled' },
  CONFIRMED: { label: 'Confirmada', class: 'badge-confirmed' },
  COMPLETED: { label: 'Completada', class: 'badge-completed' },
  CANCELLED: { label: 'Cancelada', class: 'badge-cancelled' },
  NO_SHOW: { label: 'No Asistió', class: 'badge-no-show' }
};

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [cancelReason, setCancelReason] = useState('');
  const [formData, setFormData] = useState({
    patientId: '',
    doctorId: '',
    officeId: '',
    appointmentTypeId: '',
    startAt: '',
    endAt: '',
    observations: ''
  });
  
  // Datos para selects
  const [doctors, setDoctors] = useState([]);
  const [patients, setPatients] = useState([]);
  const [offices, setOffices] = useState([]);
  const [appointmentTypes, setAppointmentTypes] = useState([]);
  const [message, setMessage] = useState({ show: false, text: '', type: 'success' });

  useEffect(() => {
    loadAppointments();
    loadSelectData();
  }, [page]);

  const loadAppointments = async () => {
    setLoading(true);
    try {
      const response = await appointmentService.getAll(page, 10);
      setAppointments(response.data.content);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error('Error loading appointments:', error);
      showMessage('Error al cargar las citas', 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadSelectData = async () => {
    try {
      const [doctorsRes, patientsRes, officesRes, typesRes] = await Promise.all([
        doctorService.getAll(0, 100),
        patientService.getAll(0, 100),
        officeService.getAll(0, 100),
        appointmentTypeService.getAll(0, 100)
      ]);

      setDoctors(Array.isArray(doctorsRes.data) ? doctorsRes.data : doctorsRes.data.content ?? []);
      setPatients(patientsRes.data.content ?? []);
      setOffices(officesRes.data.content ?? []);
      setAppointmentTypes(typesRes.data.content ?? []);
    } catch (error) {
      console.error('Error loading select data:', error);
    }
  };
  
  const showMessage = (text, type = 'success') => {
    setMessage({ show: true, text, type });
    setTimeout(() => setMessage({ show: false, text: '', type: 'success' }), 3000);
  };

  const handleCreate = async () => {
    try {
      await appointmentService.create(formData);
      showMessage('Cita creada exitosamente');
      setShowModal(false);
      resetForm();
      loadAppointments();
    } catch (error) {
      showMessage(error.response?.data?.message || 'Error al crear la cita', 'error');
    }
  };

  const handleConfirm = async (id) => {
    try {
      await appointmentService.confirm(id);
      showMessage('Cita confirmada');
      loadAppointments();
    } catch (error) {
      showMessage('Error al confirmar la cita', 'error');
    }
  };

  const handleCancel = async () => {
    if (!cancelReason.trim()) {
      showMessage('Ingrese un motivo de cancelación', 'error');
      return;
    }
    try {
      await appointmentService.cancel(selectedAppointment.id, cancelReason);
      showMessage('Cita cancelada');
      setShowCancelModal(false);
      setSelectedAppointment(null);
      setCancelReason('');
      loadAppointments();
    } catch (error) {
      showMessage('Error al cancelar la cita', 'error');
    }
  };

  const handleComplete = async (id) => {
    try {
      await appointmentService.complete(id);
      showMessage('Cita completada');
      loadAppointments();
    } catch (error) {
      showMessage('Error al completar la cita', 'error');
    }
  };

  const handleNoShow = async (id) => {
    try {
      await appointmentService.markNoShow(id);
      showMessage('Paciente marcado como no asistió');
      loadAppointments();
    } catch (error) {
      showMessage('Error al marcar como no asistió', 'error');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Eliminar esta cita permanentemente?')) {
      try {
        await appointmentService.delete(id);
        showMessage('Cita eliminada');
        loadAppointments();
      } catch (error) {
        showMessage('Error al eliminar la cita', 'error');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      patientId: '',
      doctorId: '',
      officeId: '',
      appointmentTypeId: '',
      startAt: '',
      endAt: '',
      observations: ''
    });
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="appointments-page">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1>Citas Médicas</h1>
          <p>Gestione todas las citas de la clínica</p>
        </div>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          + Nueva Cita
        </button>
      </div>

      {/* Toast Message */}
      {message.show && (
        <div className="toast-message" style={{
          background: message.type === 'success' ? '#059669' : '#dc2626'
        }}>
          {message.text}
        </div>
      )}

      {/* Table */}
      <div className="table-container">
        <table className="appointments-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Paciente</th>
              <th>Médico</th>
              <th>Consultorio</th>
              <th>Tipo</th>
              <th>Inicio</th>
              <th>Fin</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="9"><div className="loading-spinner"><div className="spinner"></div></div></td></tr>
            ) : appointments.length === 0 ? (
              <tr><td colSpan="9"><div className="empty-state"><div className="empty-state-icon">📋</div><div className="empty-state-text">No hay citas registradas</div></div></td></tr>
            ) : (
              appointments.map(app => (
                <tr key={app.id}>
                  <td><strong style={{ color: '#006494' }}>#{app.id}</strong></td>
                  <td>{app.patientName || `ID: ${app.patientId}`}</td>
                  <td>{app.doctorName || `ID: ${app.doctorId}`}</td>
                  <td>{app.officeName || `ID: ${app.officeId}`}</td>
                  <td>{app.appointmentTypeName || `ID: ${app.appointmentTypeId}`}</td>
                  <td>{formatDateTime(app.startAt)}</td>
                  <td>{formatDateTime(app.endAt)}</td>
                  <td><span className={`badge ${statusConfig[app.status]?.class || 'badge-scheduled'}`}>{statusConfig[app.status]?.label || app.status}</span></td>
                  <td>
                    <div className="action-buttons">
                      {app.status === 'SCHEDULED' && (
                        <button className="action-btn confirm" onClick={() => handleConfirm(app.id)}>✓ Confirmar</button>
                      )}
                      {(app.status === 'SCHEDULED' || app.status === 'CONFIRMED') && (
                        <>
                          <button className="action-btn complete" onClick={() => handleComplete(app.id)}>✔ Completar</button>
                          <button className="action-btn cancel" onClick={() => { setSelectedAppointment(app); setShowCancelModal(true); }}>✗ Cancelar</button>
                        </>
                      )}
                      {app.status === 'CONFIRMED' && (
                        <button className="action-btn no-show" onClick={() => handleNoShow(app.id)}>⊘ No Asistió</button>
                      )}
                      <button className="action-btn delete" onClick={() => handleDelete(app.id)}>🗑 Eliminar</button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}>Anterior</button>
          {[...Array(Math.min(totalPages, 10))].map((_, i) => (
            <button key={i} className={page === i ? 'active' : ''} onClick={() => setPage(i)}>{i + 1}</button>
          ))}
          <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page === totalPages - 1}>Siguiente</button>
        </div>
      )}

      {/* Create Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-container" onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h2>Nueva Cita</h2></div>
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Paciente</label>
                <select className="form-select" value={formData.patientId} onChange={e => setFormData({ ...formData, patientId: e.target.value })}>
                  <option value="">Seleccionar</option>
                  {patients.map(p => <option key={p.id} value={p.id}>{p.firstName} {p.lastName}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Médico</label>
                <select className="form-select" value={formData.doctorId} onChange={e => setFormData({ ...formData, doctorId: e.target.value })}>
                  <option value="">Seleccionar</option>
                  {doctors.map(d => <option key={d.id} value={d.id}>{d.firstName} {d.lastName}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Consultorio</label>
                <select className="form-select" value={formData.officeId} onChange={e => setFormData({ ...formData, officeId: e.target.value })}>
                  <option value="">Seleccionar</option>
                  {offices.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Tipo de Cita</label>
                <select className="form-select" value={formData.appointmentTypeId} onChange={e => setFormData({ ...formData, appointmentTypeId: e.target.value })}>
                  <option value="">Seleccionar</option>
                  {appointmentTypes.map(t => <option key={t.id} value={t.id}>{t.name} ({t.durationMinutes} min)</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Inicio</label>
                <input type="datetime-local" className="form-input" value={formData.startAt} onChange={e => setFormData({ ...formData, startAt: e.target.value })} />
              </div>
              <div className="form-group">
                <label className="form-label">Fin</label>
                <input type="datetime-local" className="form-input" value={formData.endAt} onChange={e => setFormData({ ...formData, endAt: e.target.value })} />
              </div>
              <div className="form-group">
                <label className="form-label">Observaciones</label>
                <textarea className="form-textarea" rows="3" value={formData.observations} onChange={e => setFormData({ ...formData, observations: e.target.value })} />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-outline" onClick={() => setShowModal(false)}>Cancelar</button>
              <button className="btn-primary" onClick={handleCreate}>Guardar</button>
            </div>
          </div>
        </div>
      )}

      {/* Cancel Modal */}
      {showCancelModal && (
        <div className="modal-overlay" onClick={() => setShowCancelModal(false)}>
          <div className="modal-container" onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h2>Cancelar Cita</h2></div>
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Motivo de cancelación</label>
                <textarea className="form-textarea" rows="3" value={cancelReason} onChange={e => setCancelReason(e.target.value)} placeholder="Describa el motivo..." />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-outline" onClick={() => setShowCancelModal(false)}>Volver</button>
              <button className="btn-primary" style={{ background: '#dc2626' }} onClick={handleCancel}>Cancelar Cita</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}