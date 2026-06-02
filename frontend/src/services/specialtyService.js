import api from './api';

export const specialtyService = {
  getAll: (page = 0, size = 100) =>
    api.get(`/specialties?page=${page}&size=${size}`),

  create: (data) =>
    api.post('/specialties', data),

  delete: (id) =>
    api.delete(`/specialties/${id}`),
};