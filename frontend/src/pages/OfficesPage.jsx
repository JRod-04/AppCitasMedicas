import React, { useState, useEffect } from 'react';
import { officeService } from '../services/api';
import { Plus, Edit, Trash2, X, Check } from 'lucide-react';
import './OfficesPage.css';

const statusConfig = {
  ACTIVE: { label: 'Activo', class: 'status-active' },
  INACTIVE: { label: 'Inactivo', class: 'status-inactive' },
  MAINTENANCE: { label: 'Mantenimiento', class: 'status-maintenance' }
};

export default function OfficesPage() {
  const [offices, setOffices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [editingOffice, setEditingOffice] = useState(null);
  const [message, setMessage] = useState({ show: false, text: '', type: 'success' });
  const [formData, setFormData] = useState({
    name: '',
    location: '',
    floor: '',
    status: 'ACTIVE'
  });

  useEffect(() => {
    loadOffices();
  }, [page]);

  const loadOffices = async () => {
    setLoading(true);
    try {
      const response = await officeService.getAll(page, 10);
      setOffices(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      showMessage('Error al cargar los consultorios', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showMessage = (text, type = 'success') => {
    setMessage({ show: true, text, type });
    setTimeout(() => setMessage({ show: false, text: '', type: 'success' }), 3000);
  };

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      showMessage('El nombre del consultorio es obligatorio', 'error');
      return;
    }

    try {
      if (editingOffice) {
        await officeService.update(editingOffice.id, formData);
        showMessage('Consultorio actualizado exitosamente');
      } else {
        await officeService.create(formData);
        showMessage('Consultorio creado exitosamente');
      }
      setShowModal(false);
      resetForm();
      loadOffices();
    } catch (error) {
      showMessage(error.response?.data?.message || 'Error al guardar', 'error');
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`¿Eliminar el consultorio "${name}"?`)) {
      try {
        await officeService.delete(id);
        showMessage('Consultorio eliminado');
        loadOffices();
      } catch (error) {
        showMessage('Error al eliminar', 'error');
      }
    }
  };

  const handleEdit = (office) => {
    setEditingOffice(office);
    setFormData({
      name: office.name,
      location: office.location || '',
      floor: office.floor || '',
      status: office.status
    });
    setShowModal(true);
  };

  const resetForm = () => {
    setEditingOffice(null);
    setFormData({
      name: '',
      location: '',
      floor: '',
      status: 'ACTIVE'
    });
  };

  return (
    <div className="offices-page">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-heading">Consultorios</h1>
          <p className="page-description">Gestione los consultorios de la clínica</p>
        </div>
        <button className="btn-primary" onClick={() => { resetForm(); setShowModal(true); }}>
          <Plus size={18} /> Nuevo Consultorio
        </button>
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
              <th>ID</th>
              <th>Nombre</th>
              <th>Ubicación</th>
              <th>Piso</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="6"><div className="loading-state">Cargando...</div></td></tr>
            ) : offices.length === 0 ? (
              <tr><td colSpan="6"><div className="empty-state">No hay consultorios registrados</div></td></tr>
            ) : (
              offices.map(office => (
                <tr key={office.id}>
                  <td className="id-cell">#{office.id}</td>
                  <td className="name-cell">{office.name}</td>
                  <td>{office.location || '—'}</td>
                  <td>{office.floor || '—'}</td>
                  <td>
                    <span className={`status-badge ${statusConfig[office.status]?.class || 'status-active'}`}>
                      {statusConfig[office.status]?.label || office.status}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button className="action-btn edit" onClick={() => handleEdit(office)}>
                        <Edit size={16} />
                      </button>
                      <button className="action-btn delete" onClick={() => handleDelete(office.id, office.name)}>
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
            Anterior
          </button>
          {[...Array(totalPages)].map((_, i) => (
            <button key={i} className={page === i ? 'active' : ''} onClick={() => setPage(i)}>
              {i + 1}
            </button>
          ))}
          <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page === totalPages - 1}>
            Siguiente
          </button>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingOffice ? 'Editar Consultorio' : 'Nuevo Consultorio'}</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>
                <X size={20} />
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>Nombre *</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.name}
                  onChange={e => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Ej: Consultorio A, Sala de Emergencias"
                />
              </div>
              <div className="form-group">
                <label>Ubicación</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.location}
                  onChange={e => setFormData({ ...formData, location: e.target.value })}
                  placeholder="Ej: Primer piso, ala norte"
                />
              </div>
              <div className="form-group">
                <label>Piso</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.floor}
                  onChange={e => setFormData({ ...formData, floor: e.target.value })}
                  placeholder="Ej: 1, 2, 3"
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
                  <option value="MAINTENANCE">Mantenimiento</option>
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-outline" onClick={() => setShowModal(false)}>Cancelar</button>
              <button className="btn-primary" onClick={handleSubmit}>
                {editingOffice ? 'Actualizar' : 'Crear'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}