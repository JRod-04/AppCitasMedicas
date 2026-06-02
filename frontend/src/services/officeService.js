import api from './api';

export const officeService = {
  getAll: (page = 0, size = 100) =>
    api.get(`/offices?page=${page}&size=${size}`),

  create: (data) =>
    api.post('/offices', data),

  update: (id, data) =>
    api.patch(`/offices/${id}`, data),

  delete: (id) =>
    api.delete(`/offices/${id}`),
};