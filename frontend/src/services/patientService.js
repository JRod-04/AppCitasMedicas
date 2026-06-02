import api from './api';

export const patientService = {
  getAll: (page = 0, size = 100) =>
    api.get(`/patients?page=${page}&size=${size}`),

  getById: (id) =>
    api.get(`/patients/${id}`),

  create: (data) =>
    api.post('/patients', data),

  update: (id, data) =>
    api.patch(`/patients/${id}`, data),

  delete: (id) =>
    api.delete(`/patients/${id}`),
};