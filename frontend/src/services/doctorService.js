import api from './api';

export const doctorService = {
  getAll: (page = 0, size = 100, specialtyId = null) => {
    const url = specialtyId
      ? `/doctors?specialtyId=${specialtyId}&page=${page}&size=${size}`
      : `/doctors?page=${page}&size=${size}`;
    return api.get(url);
  },

  getById: (id) =>
    api.get(`/doctors/${id}`),

  create: (data) =>
    api.post('/doctors', data),

  update: (id, data) =>
    api.patch(`/doctors/${id}`, data),

  delete: (id) =>
    api.delete(`/doctors/${id}`),
};