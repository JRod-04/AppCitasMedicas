import React, { useState, useEffect } from 'react';
import { doctorService, availabilityService, appointmentTypeService } from '../services/api';
import { Search, Clock, CheckCircle, XCircle } from 'lucide-react';
import './AvailabilityPage.css';

export default function AvailabilityPage() {
  const [doctors, setDoctors] = useState([]);
  const [appointmentTypes, setAppointmentTypes] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [selectedTypeId, setSelectedTypeId] = useState('');
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ show: false, text: '', type: 'success' });

  useEffect(() => { loadDoctors(); loadTypes(); }, []);

  const loadDoctors = async () => {
    try { 
      const res = await doctorService.getAll(0, 100); 
      setDoctors(res.data.content || res.data || []); 
    } catch (error) { 
      console.error('Error loading doctors:', error); 
    }
  };

  const loadTypes = async () => {
    try { 
      const res = await appointmentTypeService.getAll(0, 100); 
      setAppointmentTypes(res.data.content || res.data || []); 
    } catch (error) { 
      console.error('Error loading types:', error); 
    }
  };

  const searchAvailability = async () => {
    if (!selectedDoctor || !selectedDate || !selectedTypeId) {
      showMessage('Seleccione doctor, fecha y tipo de cita', 'error');
      return;
    }
    setLoading(true);
    try {
      const res = await availabilityService.getAvailableSlots(
        selectedDoctor.id, 
        selectedDate, 
        selectedTypeId, 
        0, 
        50
      );
      // La API solo devuelve slots disponibles
      const availableSlots = res.data.content || res.data || [];
      setSlots(availableSlots);
      console.log('Slots disponibles encontrados:', availableSlots.length);
    } catch (error) { 
      console.error('Error al cargar disponibilidad:', error.response?.data);
      showMessage(error.response?.data?.message || 'Error al cargar disponibilidad', 'error'); 
      setSlots([]); 
    } finally { 
      setLoading(false); 
    }
  };

  const showMessage = (text, type = 'success') => {
    setMessage({ show: true, text, type });
    setTimeout(() => setMessage({ show: false, text: '', type: 'success' }), 3000);
  };

  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  };

  const handleReserve = (slot) => {
    // Aquí puedes redirigir al formulario de crear cita con los datos precargados
    showMessage(`Seleccionado slot: ${formatTime(slot.startAt)}`, 'success');
    // window.location.href = `/appointments?doctorId=${selectedDoctor.id}&startAt=${slot.startAt}&endAt=${slot.endAt}`;
  };

  return (
    <div className="availability-page">
      <div className="page-header">
        <div>
          <h1 className="page-heading">Disponibilidad</h1>
          <p className="page-description">Consulta los slots disponibles por doctor y fecha</p>
        </div>
      </div>

      {message.show && <div className={`toast-message ${message.type}`}>{message.text}</div>}

      <div className="filters-card">
        <div className="filters-grid">
          <div className="form-group">
            <label>Doctor</label>
            <select 
              className="form-select" 
              value={selectedDoctor?.id || ''} 
              onChange={e => setSelectedDoctor(doctors.find(d => d.id === parseInt(e.target.value)))}
            >
              <option value="">Seleccionar doctor</option>
              {doctors.map(d => (
                <option key={d.id} value={d.id}>
                  Dr/a. {d.firstName} {d.lastName} - {d.specialtyName}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Fecha</label>
            <input 
              type="date" 
              className="form-input" 
              value={selectedDate} 
              onChange={e => setSelectedDate(e.target.value)} 
            />
          </div>
          <div className="form-group">
            <label>Tipo de cita</label>
            <select 
              className="form-select" 
              value={selectedTypeId} 
              onChange={e => setSelectedTypeId(e.target.value)}
            >
              <option value="">Seleccionar tipo</option>
              {appointmentTypes.map(t => (
                <option key={t.id} value={t.id}>
                  {t.name} ({t.durationMinutes} min)
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <button className="btn-primary" onClick={searchAvailability}>
              <Search size={18} /> Buscar disponibilidad
            </button>
          </div>
        </div>
      </div>

      {selectedDoctor && slots.length > 0 && (
        <div className="results-card">
          <div className="results-header">
            <h2>Dr/a. {selectedDoctor.firstName} {selectedDoctor.lastName}</h2>
            <p className="results-date">{formatDate(selectedDate)}</p>
          </div>
          <div className="results-stats">
            <span className="stat-available">{slots.length} disponibles</span>
          </div>
          <div className="slots-grid">
            {slots.map((slot, idx) => (
              <button key={idx} className="slot-btn available" onClick={() => handleReserve(slot)}>
                <Clock size={14} /> {formatTime(slot.startAt)}
              </button>
            ))}
          </div>
        </div>
      )}

      {selectedDoctor && slots.length === 0 && !loading && (
        <div className="empty-state">
          No hay horarios disponibles para los criterios seleccionados
        </div>
      )}
    </div>
  );
}