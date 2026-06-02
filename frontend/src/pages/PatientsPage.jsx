import React, { useState, useEffect } from 'react';
import { patientService } from '../services/api';
import { Plus, Edit, Trash2, X, Search, ChevronLeft, ChevronRight } from 'lucide-react';
import './PatientsPage.css';

const statusConfig = {
  ACTIVE: { label: 'Activo', class: 'status-active' },
  INACTIVE: { label: 'Inactivo', class: 'status-inactive' }
};

export default function PatientsPage() {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingPatient, setEditingPatient] = useState(null);
  const [message, setMessage] = useState({ show: false, text: '', type: 'success' });
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    documentNumber: '',
    email: '',
    phone: '',
    status: 'ACTIVE'
  });

  useEffect(() => {
    loadPatients();
  }, [page]);

  const loadPatients = async () => {
    setLoading(true);
    try {
      const response = await patientService.getAll(page, 10);
      const content = response.data.content || response.data || [];
      setPatients(content);
      setTotalPages(response.data.totalPages || 1);
      setTotalElements(response.data.totalElements || content.length);
    } catch (error) {
      console.error('Error loading patients:', error);
      showMessage('Error al cargar los pacientes', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showMessage = (text, type = 'success') => {
    setMessage({ show: true, text, type });
    setTimeout(() => setMessage({ show: false, text: '', type: 'success' }), 3000);
  };

  const handleSubmit = async () => {
    if (!formData.firstName.trim() || !formData.lastName.trim() || !formData.documentNumber.trim()) {
      showMessage('Nombre, apellido y documento son obligatorios', 'error');
      return;
    }

    try {
      if (editingPatient) {
        await patientService.update(editingPatient.id, formData);
        showMessage('Paciente actualizado exitosamente');
      } else {
        await patientService.create(formData);
        showMessage('Paciente creado exitosamente');
      }
      setShowModal(false);
      resetForm();
      loadPatients();
    } catch (error) {
      showMessage(error.response?.data?.message || 'Error al guardar', 'error');
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`¿Eliminar al paciente "${name}"?`)) {
      try {
        await patientService.delete(id);
        showMessage('Paciente eliminado');
        loadPatients();
      } catch (error) {
        showMessage('Error al eliminar', 'error');
      }
    }
  };

  const handleEdit = (patient) => {
    setEditingPatient(patient);
    setFormData({
      firstName: patient.firstName,
      lastName: patient.lastName,
      documentNumber: patient.documentNumber,
      email: patient.email || '',
      phone: patient.phone || '',
      status: patient.status
    });
    setShowModal(true);
  };

  const resetForm = () => {
    setEditingPatient(null);
    setFormData({
      firstName: '',
      lastName: '',
      documentNumber: '',
      email: '',
      phone: '',
      status: 'ACTIVE'
    });
  };

  // Filtrar pacientes por búsqueda
  const filteredPatients = patients.filter(patient =>
    searchTerm === '' ||
    `${patient.firstName} ${patient.lastName}`.toLowerCase().includes(searchTerm.toLowerCase()) ||
    patient.documentNumber?.includes(searchTerm) ||
    patient.email?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const activeCount = patients.filter(p => p.status === 'ACTIVE').length;

  return (
    <div className="patients-page">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-heading">Pacientes</h1>
          <p className="page-description">
            {activeCount} pacientes activos · {totalElements} en total
          </p>
        </div>
        <button className="btn-primary" onClick={() => { resetForm(); setShowModal(true); }}>
          <Plus size={18} /> Nuevo Paciente
        </button>
      </div>

      {/* Search Bar */}
      <div className="search-bar">
        <Search size={18} className="search-icon" />
        <input
          type="text"
          className="search-input"
          placeholder="Buscar por nombre, ID o email..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {/* Toast Message */}
      {message.show && (
        <div className={`toast-message ${message.type}`}>
          {message.text}
        </div>
      )}

      {/* Table */}
      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>NOMBRE</th>
              <th>IDENTIFICACIÓN</th>
              <th>CONTACTO</th>
              <th>ESTADO</th>
              <th>ACCIONES</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="5"><div className="loading-state">Cargando...</div></td></tr>
            ) : filteredPatients.length === 0 ? (
              <tr><td colSpan="5"><div className="empty-state">No hay pacientes registrados</div></td></tr>
            ) : (
              filteredPatients.map(patient => (
                <tr key={patient.id}>
                  <td className="name-cell">
                    <div className="patient-name">
                      {patient.firstName} {patient.lastName}
                    </div>
                    <div className="patient-email">{patient.email || '—'}</div>
                  </td>
                  <td className="document-cell">{patient.documentNumber}</td>
                  <td className="contact-cell">
                    {patient.phone || '—'}
                  </td>
                  <td>
                    <span className={`status-badge ${statusConfig[patient.status]?.class || 'status-active'}`}>
                      {statusConfig[patient.status]?.label || patient.status}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button className="action-btn edit" onClick={() => handleEdit(patient)}>
                        <Edit size={16} />
                      </button>
                      <button className="action-btn delete" onClick={() => handleDelete(patient.id, `${patient.firstName} ${patient.lastName}`)}>
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

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}>
            <ChevronLeft size={16} /> Anterior
          </button>
          <div className="pagination-pages">
            {[...Array(Math.min(totalPages, 5))].map((_, i) => {
              let pageNum = i;
              if (totalPages > 5 && page > 2) {
                pageNum = page - 2 + i;
                if (pageNum >= totalPages) return null;
              }
              return (
                <button
                  key={i}
                  className={`page-btn ${page === pageNum ? 'active' : ''}`}
                  onClick={() => setPage(pageNum)}
                >
                  {pageNum + 1}
                </button>
              );
            })}
          </div>
          <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page === totalPages - 1}>
            Siguiente <ChevronRight size={16} />
          </button>
        </div>
      )}

      {/* Modal - Create/Edit */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingPatient ? 'Editar Paciente' : 'Nuevo Paciente'}</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>
                <X size={20} />
              </button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group">
                  <label>Nombre *</label>
                  <input
                    type="text"
                    className="form-input"
                    value={formData.firstName}
                    onChange={e => setFormData({ ...formData, firstName: e.target.value })}
                    placeholder="Nombre"
                  />
                </div>
                <div className="form-group">
                  <label>Apellido *</label>
                  <input
                    type="text"
                    className="form-input"
                    value={formData.lastName}
                    onChange={e => setFormData({ ...formData, lastName: e.target.value })}
                    placeholder="Apellido"
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Documento *</label>
                  <input
                    type="text"
                    className="form-input"
                    value={formData.documentNumber}
                    onChange={e => setFormData({ ...formData, documentNumber: e.target.value })}
                    placeholder="Número de identificación"
                  />
                </div>
                <div className="form-group">
                  <label>Teléfono</label>
                  <input
                    type="text"
                    className="form-input"
                    value={formData.phone}
                    onChange={e => setFormData({ ...formData, phone: e.target.value })}
                    placeholder="Teléfono de contacto"
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  className="form-input"
                  value={formData.email}
                  onChange={e => setFormData({ ...formData, email: e.target.value })}
                  placeholder="correo@ejemplo.com"
                />
              </div>
              <div className="form-group">
                <label>Estado</label>
                <select
                  className="form-select"
                  value={formData.status}
                  onChange={e => setFormData({ ...formData, status: e.target.value })}
                >
                  <option value="ACTIVE">Activo</option>
                  <option value="INACTIVE">Inactivo</option>
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-outline" onClick={() => setShowModal(false)}>Cancelar</button>
              <button className="btn-primary" onClick={handleSubmit}>
                {editingPatient ? 'Actualizar' : 'Crear'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}