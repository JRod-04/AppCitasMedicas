// src/pages/CatalogPage.jsx
import React, { useState, useEffect } from 'react';
import { specialtyService, appointmentTypeService } from '../services/api';
import { Plus, Edit, Trash2, X } from 'lucide-react';
import './CatalogPage.css';

export default function CatalogPage() {
  const [specialties, setSpecialties] = useState([]);
  const [appointmentTypes, setAppointmentTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showSpecialtyModal, setShowSpecialtyModal] = useState(false);
  const [showTypeModal, setShowTypeModal] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [message, setMessage] = useState({ show: false, text: '', type: 'success' });
  const [specialtyForm, setSpecialtyForm] = useState({ name: '', description: '' });
  const [typeForm, setTypeForm] = useState({ name: '', durationMinutes: '', description: '' });

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [specRes, typesRes] = await Promise.all([
        specialtyService.getAll(0, 100),
        appointmentTypeService.getAll(0, 100)
      ]);
      setSpecialties(specRes.data.content || specRes.data || []);
      setAppointmentTypes(typesRes.data.content || typesRes.data || []);
    } catch (error) { showMessage('Error al cargar datos', 'error'); }
    finally { setLoading(false); }
  };

  const showMessage = (text, type = 'success') => {
    setMessage({ show: true, text, type });
    setTimeout(() => setMessage({ show: false, text: '', type: 'success' }), 3000);
  };

  const handleCreateSpecialty = async () => {
    if (!specialtyForm.name.trim()) { showMessage('El nombre es obligatorio', 'error'); return; }
    try {
      await specialtyService.create(specialtyForm);
      showMessage('Especialidad creada');
      setShowSpecialtyModal(false);
      setSpecialtyForm({ name: '', description: '' });
      loadData();
    } catch (error) { showMessage('Error al crear', 'error'); }
  };

  const handleDeleteSpecialty = async (id, name) => {
    if (window.confirm(`¿Eliminar la especialidad "${name}"?`)) {
      try { await specialtyService.delete(id); showMessage('Especialidad eliminada'); loadData(); }
      catch (error) { showMessage('Error al eliminar', 'error'); }
    }
  };

  const handleCreateType = async () => {
    if (!typeForm.name.trim() || !typeForm.durationMinutes) { showMessage('Nombre y duración son obligatorios', 'error'); return; }
    try {
      await appointmentTypeService.create(typeForm);
      showMessage('Tipo de cita creado');
      setShowTypeModal(false);
      setTypeForm({ name: '', durationMinutes: '', description: '' });
      loadData();
    } catch (error) { showMessage('Error al crear', 'error'); }
  };

  const handleDeleteType = async (id, name) => {
    if (window.confirm(`¿Eliminar el tipo de cita "${name}"?`)) {
      try { await appointmentTypeService.delete(id); showMessage('Tipo eliminado'); loadData(); }
      catch (error) { showMessage('Error al eliminar', 'error'); }
    }
  };

  const getSpecialtyCode = (name) => {
    const codes = { 'Medicina General': 'MG', 'Psicología': 'PSI', 'Fisioterapia': 'FIS', 'Nutrición': 'NUT' };
    return codes[name] || name?.substring(0, 3).toUpperCase() || 'ESP';
  };

  return (
    <div className="catalog-page">
      <div className="page-header"><div><h1 className="page-heading">Catálogo</h1><p className="page-description">Gestión de especialidades médicas y tipos de cita</p></div></div>

      {message.show && <div className={`toast-message ${message.type}`}>{message.text}</div>}

      <div className="catalog-grid">
        {/* Especialidades */}
        <div className="catalog-card">
          <div className="catalog-card-header"><h2>Especialidades</h2><span className="card-badge">{specialties.length} registradas</span></div>
          <div className="catalog-items">
            {specialties.map(spec => (
              <div key={spec.id} className="catalog-item">
                <div className="item-code">{getSpecialtyCode(spec.name)}</div>
                <div className="item-info"><div className="item-name">{spec.name}</div><div className="item-desc">Código: {getSpecialtyCode(spec.name)}</div></div>
                <button className="item-delete" onClick={() => handleDeleteSpecialty(spec.id, spec.name)}><Trash2 size={16} /></button>
              </div>
            ))}
            <button className="add-item-btn" onClick={() => setShowSpecialtyModal(true)}><Plus size={16} /> Agregar especialidad</button>
          </div>
        </div>

        {/* Tipos de cita */}
        <div className="catalog-card">
          <div className="catalog-card-header"><h2>Tipos de cita</h2><span className="card-badge">{appointmentTypes.length} configurados</span></div>
          <div className="catalog-items">
            {appointmentTypes.map(type => (
              <div key={type.id} className="catalog-item">
                <div className="item-info"><div className="item-name">{type.name}</div><div className="item-desc">Duración: {type.durationMinutes} min</div></div>
                <button className="item-delete" onClick={() => handleDeleteType(type.id, type.name)}><Trash2 size={16} /></button>
              </div>
            ))}
            <button className="add-item-btn" onClick={() => setShowTypeModal(true)}><Plus size={16} /> Agregar tipo de cita</button>
          </div>
        </div>
      </div>

      {/* Modal Especialidad */}
      {showSpecialtyModal && (<div className="modal-overlay" onClick={() => setShowSpecialtyModal(false)}><div className="modal" onClick={e => e.stopPropagation()}><div className="modal-header"><h2>Nueva Especialidad</h2><button className="modal-close" onClick={() => setShowSpecialtyModal(false)}><X size={20} /></button></div><div className="modal-body"><div className="form-group"><label>Nombre *</label><input type="text" className="form-input" value={specialtyForm.name} onChange={e => setSpecialtyForm({ ...specialtyForm, name: e.target.value })} placeholder="Ej: Cardiología" /></div><div className="form-group"><label>Descripción</label><textarea className="form-textarea" rows="3" value={specialtyForm.description} onChange={e => setSpecialtyForm({ ...specialtyForm, description: e.target.value })} placeholder="Descripción de la especialidad" /></div></div><div className="modal-footer"><button className="btn-outline" onClick={() => setShowSpecialtyModal(false)}>Cancelar</button><button className="btn-primary" onClick={handleCreateSpecialty}>Crear</button></div></div></div>)}

      {/* Modal Tipo de Cita */}
      {showTypeModal && (<div className="modal-overlay" onClick={() => setShowTypeModal(false)}><div className="modal" onClick={e => e.stopPropagation()}><div className="modal-header"><h2>Nuevo Tipo de Cita</h2><button className="modal-close" onClick={() => setShowTypeModal(false)}><X size={20} /></button></div><div className="modal-body"><div className="form-group"><label>Nombre *</label><input type="text" className="form-input" value={typeForm.name} onChange={e => setTypeForm({ ...typeForm, name: e.target.value })} placeholder="Ej: Consulta General" /></div><div className="form-group"><label>Duración (minutos) *</label><input type="number" className="form-input" value={typeForm.durationMinutes} onChange={e => setTypeForm({ ...typeForm, durationMinutes: e.target.value })} placeholder="30" /></div><div className="form-group"><label>Descripción</label><textarea className="form-textarea" rows="3" value={typeForm.description} onChange={e => setTypeForm({ ...typeForm, description: e.target.value })} placeholder="Descripción del tipo de cita" /></div></div><div className="modal-footer"><button className="btn-outline" onClick={() => setShowTypeModal(false)}>Cancelar</button><button className="btn-primary" onClick={handleCreateType}>Crear</button></div></div></div>)}
    </div>
  );
}