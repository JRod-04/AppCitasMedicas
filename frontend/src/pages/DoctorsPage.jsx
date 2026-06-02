// src/pages/DoctorsPage.jsx
import React, { useState, useEffect } from 'react';
import { doctorService, specialtyService } from '../services/api';
import { Plus, Edit, Trash2, X, Search, Calendar, ChevronLeft, ChevronRight } from 'lucide-react';
import './DoctorsPage.css';

const statusConfig = {
  true: { label: 'Activo', class: 'status-active' },
  false: { label: 'Inactivo', class: 'status-inactive' }
};

export default function DoctorsPage() {
  const [doctors, setDoctors] = useState([]);
  const [specialties, setSpecialties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [editingDoctor, setEditingDoctor] = useState(null);
  const [message, setMessage] = useState({ show: false, text: '', type: 'success' });
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    licenseNumber: '',
    email: '',
    specialtyId: '',
    active: true
  });
  const [scheduleForm, setScheduleForm] = useState({
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '17:00'
  });

  const daysOfWeek = [
    'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
  ];

  const dayLabels = {
    MONDAY: 'Lunes', TUESDAY: 'Martes', WEDNESDAY: 'Miércoles',
    THURSDAY: 'Jueves', FRIDAY: 'Viernes', SATURDAY: 'Sábado', SUNDAY: 'Domingo'
  };

  useEffect(() => {
    loadDoctors();
    loadSpecialties();
  }, [page]);

  const loadDoctors = async () => {
    setLoading(true);
    try {
      const response = await doctorService.getAll(page, 10);
      const content = response.data.content || response.data || [];
      setDoctors(content);
      setTotalPages(response.data.totalPages || 1);
    } catch (error) {
      showMessage('Error al cargar los doctores', 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadSpecialties = async () => {
    try {
      const response = await specialtyService.getAll(0, 100);
      setSpecialties(response.data.content || response.data || []);
    } catch (error) {
      console.error('Error loading specialties:', error);
    }
  };

  const loadSchedules = async (doctorId) => {
    try {
      const response = await doctorService.getSchedules(doctorId, 0, 20);
      setSchedules(response.data.content || response.data || []);
    } catch (error) {
      console.error('Error loading schedules:', error);
      setSchedules([]);
    }
  };

  const showMessage = (text, type = 'success') => {
    setMessage({ show: true, text, type });
    setTimeout(() => setMessage({ show: false, text: '', type: 'success' }), 3000);
  };

  const handleSubmit = async () => {
    if (!formData.firstName.trim() || !formData.lastName.trim() || !formData.licenseNumber.trim() || !formData.specialtyId) {
      showMessage('Nombre, apellido, licencia y especialidad son obligatorios', 'error');
      return;
    }

    try {
      if (editingDoctor) {
        await doctorService.update(editingDoctor.id, formData);
        showMessage('Doctor actualizado exitosamente');
      } else {
        await doctorService.create(formData);
        showMessage('Doctor creado exitosamente');
      }
      setShowModal(false);
      resetForm();
      loadDoctors();
    } catch (error) {
      showMessage(error.response?.data?.message || 'Error al guardar', 'error');
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`¿Eliminar al doctor "${name}"?`)) {
      try {
        await doctorService.delete(id);
        showMessage('Doctor eliminado');
        loadDoctors();
      } catch (error) {
        showMessage('Error al eliminar', 'error');
      }
    }
  };

  const handleViewSchedules = async (doctor) => {
    setSelectedDoctor(doctor);
    await loadSchedules(doctor.id);
    setShowScheduleModal(true);
  };

  const handleAddSchedule = async () => {
    if (!selectedDoctor) return;
    try {
      await doctorService.createSchedule(selectedDoctor.id, scheduleForm);
      showMessage('Horario agregado');
      await loadSchedules(selectedDoctor.id);
      setScheduleForm({ dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00' });
    } catch (error) {
      showMessage('Error al agregar horario', 'error');
    }
  };

  const handleDeleteSchedule = async (scheduleId) => {
    if (window.confirm('¿Eliminar este horario?')) {
      try {
        await doctorService.deleteSchedule(selectedDoctor.id, scheduleId);
        showMessage('Horario eliminado');
        await loadSchedules(selectedDoctor.id);
      } catch (error) {
        showMessage('Error al eliminar horario', 'error');
      }
    }
  };

  const handleEdit = (doctor) => {
    setEditingDoctor(doctor);
    setFormData({
      firstName: doctor.firstName,
      lastName: doctor.lastName,
      licenseNumber: doctor.licenseNumber,
      email: doctor.email || '',
      specialtyId: doctor.specialtyId,
      active: doctor.active
    });
    setShowModal(true);
  };

  const resetForm = () => {
    setEditingDoctor(null);
    setFormData({
      firstName: '',
      lastName: '',
      licenseNumber: '',
      email: '',
      specialtyId: '',
      active: true
    });
  };

  const filteredDoctors = doctors.filter(doctor =>
    searchTerm === '' ||
    `${doctor.firstName} ${doctor.lastName}`.toLowerCase().includes(searchTerm.toLowerCase()) ||
    doctor.specialtyName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getInitials = (firstName, lastName) => {
    return `${firstName?.charAt(0) || ''}${lastName?.charAt(0) || ''}`;
  };

  const getSpecialtyCode = (specialtyName) => {
    const codes = {
      'Medicina General': 'MG',
      'Psicología': 'PSI',
      'Fisioterapia': 'FIS',
      'Nutrición': 'NUT'
    };
    return codes[specialtyName] || specialtyName?.substring(0, 3).toUpperCase() || 'ESP';
  };

  return (
    <div className="doctors-page">
      <div className="page-header">
        <div>
          <h1 className="page-heading">Doctores</h1>
          <p className="page-description">{doctors.filter(d => d.active).length} doctores activos</p>
        </div>
        <button className="btn-primary" onClick={() => { resetForm(); setShowModal(true); }}>
          <Plus size={18} /> Nuevo Doctor
        </button>
      </div>

      <div className="search-bar">
        <Search size={18} className="search-icon" />
        <input
          type="text"
          className="search-input"
          placeholder="Buscar por nombre o especialidad..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {message.show && <div className={`toast-message ${message.type}`}>{message.text}</div>}

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>DOCTOR</th>
              <th>ESPECIALIDAD</th>
              <th>ESTADO</th>
              <th>ACCIONES</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="4"><div className="loading-state">Cargando...</div></td></tr>
            ) : filteredDoctors.length === 0 ? (
              <tr><td colSpan="4"><div className="empty-state">No hay doctores registrados</div></td></tr>
            ) : (
              filteredDoctors.map(doctor => (
                <tr key={doctor.id}>
                  <td className="doctor-cell">
                    <div className="doctor-avatar">{getInitials(doctor.firstName, doctor.lastName)}</div>
                    <div>
                      <div className="doctor-name">Dr/a. {doctor.firstName} {doctor.lastName}</div>
                      <div className="doctor-email">{doctor.email || '—'}</div>
                    </div>
                  </td>
                  <td>
                    <span className="specialty-badge">
                      {getSpecialtyCode(doctor.specialtyName)} {doctor.specialtyName}
                    </span>
                  </td>
                  <td>
                    <span className={`status-badge ${statusConfig[doctor.active]?.class || 'status-active'}`}>
                      {doctor.active ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button className="action-btn schedule" onClick={() => handleViewSchedules(doctor)}>
                        <Calendar size={16} /> Horarios
                      </button>
                      <button className="action-btn edit" onClick={() => handleEdit(doctor)}>
                        <Edit size={16} />
                      </button>
                      <button className="action-btn delete" onClick={() => handleDelete(doctor.id, `${doctor.firstName} ${doctor.lastName}`)}>
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}>
            <ChevronLeft size={16} /> Anterior
          </button>
          {[...Array(totalPages)].map((_, i) => (
            <button key={i} className={page === i ? 'active' : ''} onClick={() => setPage(i)}>
              {i + 1}
            </button>
          ))}
          <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page === totalPages - 1}>
            Siguiente <ChevronRight size={16} />
          </button>
        </div>
      )}

      {/* Modal para crear/editar doctor */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingDoctor ? 'Editar Doctor' : 'Nuevo Doctor'}</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}><X size={20} /></button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group"><label>Nombre *</label><input type="text" className="form-input" value={formData.firstName} onChange={e => setFormData({ ...formData, firstName: e.target.value })} placeholder="Nombre" /></div>
                <div className="form-group"><label>Apellido *</label><input type="text" className="form-input" value={formData.lastName} onChange={e => setFormData({ ...formData, lastName: e.target.value })} placeholder="Apellido" /></div>
              </div>
              <div className="form-row">
                <div className="form-group"><label>N° Licencia *</label><input type="text" className="form-input" value={formData.licenseNumber} onChange={e => setFormData({ ...formData, licenseNumber: e.target.value })} placeholder="Número de licencia" /></div>
                <div className="form-group"><label>Email</label><input type="email" className="form-input" value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} placeholder="correo@ejemplo.com" /></div>
              </div>
              <div className="form-row">
                <div className="form-group"><label>Especialidad *</label><select className="form-select" value={formData.specialtyId} onChange={e => setFormData({ ...formData, specialtyId: e.target.value })}><option value="">Seleccionar</option>{specialties.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}</select></div>
                <div className="form-group"><label>Estado</label><select className="form-select" value={formData.active} onChange={e => setFormData({ ...formData, active: e.target.value === 'true' })}><option value="true">Activo</option><option value="false">Inactivo</option></select></div>
              </div>
            </div>
            <div className="modal-footer"><button className="btn-outline" onClick={() => setShowModal(false)}>Cancelar</button><button className="btn-primary" onClick={handleSubmit}>{editingDoctor ? 'Actualizar' : 'Crear'}</button></div>
          </div>
        </div>
      )}

      {/* Modal para horarios */}
      {showScheduleModal && selectedDoctor && (
        <div className="modal-overlay" onClick={() => setShowScheduleModal(false)}>
          <div className="modal modal-large" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Horarios - Dr/a. {selectedDoctor.firstName} {selectedDoctor.lastName}</h2>
              <button className="modal-close" onClick={() => setShowScheduleModal(false)}><X size={20} /></button>
            </div>
            <div className="modal-body">
              <div className="schedule-form">
                <select className="form-select" value={scheduleForm.dayOfWeek} onChange={e => setScheduleForm({ ...scheduleForm, dayOfWeek: e.target.value })}>
                  {daysOfWeek.map(day => <option key={day} value={day}>{dayLabels[day]}</option>)}
                </select>
                <input type="time" className="form-input" value={scheduleForm.startTime} onChange={e => setScheduleForm({ ...scheduleForm, startTime: e.target.value })} />
                <input type="time" className="form-input" value={scheduleForm.endTime} onChange={e => setScheduleForm({ ...scheduleForm, endTime: e.target.value })} />
                <button className="btn-primary" onClick={handleAddSchedule}>Agregar</button>
              </div>
              <div className="schedules-list">
                {schedules.map(schedule => (
                  <div key={schedule.id} className="schedule-card">
                    <span className="schedule-day">{dayLabels[schedule.dayOfWeek]}</span>
                    <span className="schedule-time">{schedule.startTime} - {schedule.endTime}</span>
                    <button className="action-btn delete" onClick={() => handleDeleteSchedule(schedule.id)}><Trash2 size={14} /></button>
                  </div>
                ))}
                {schedules.length === 0 && <div className="empty-state">No hay horarios registrados</div>}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}