import api from './api';

export const appointmentService = {
  getAll: (page = 0, size = 10) =>
    api.get(`/appointments?page=${page}&size=${size}`),

  getById: (id) =>
    api.get(`/appointments/${id}`),

  create: (data) =>
    api.post('/appointments', data),

  confirm: (id) =>
    api.put(`/appointments/${id}/confirm`),

  cancel: (id, cancellationReason) =>
    api.put(`/appointments/${id}/cancel`, { cancellationReason }),

  complete: (id, observations = null) =>
    api.put(`/appointments/${id}/complete`, observations ? { observations } : {}),

  markNoShow: (id) =>
    api.put(`/appointments/${id}/no-show`),

  update: (id, data) => 
    api.patch(`/appointments/${id}/`, data),

  delete: (id) =>
    api.delete(`/appointments/${id}`),
};