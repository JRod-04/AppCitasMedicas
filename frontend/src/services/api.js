// src/services/api.js
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' }
});

// ─── INTERCEPTOR REQUEST: agrega el token JWT a cada petición ──────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── INTERCEPTOR RESPONSE: maneja errores globalmente ──────────────────────
api.interceptors.response.use(
  (res) => res,
  (err) => {
    console.error('[API Error]', err.response?.status, err.config?.url, err.response?.data);

    // Si el token expiró o es inválido, redirige al login
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }

    return Promise.reject(err);
  }
);

// ─── HELPERS ───────────────────────────────────────────────────────────────
export const getContent  = (data) => Array.isArray(data) ? data : (data?.content ?? []);
export const getTotal    = (data) => data?.totalElements ?? (Array.isArray(data) ? data.length : 0);
export const getTotalPgs = (data) => data?.totalPages ?? 1;

// ─── AUTH ──────────────────────────────────────────────────────────────────
export const authService = {
  login: async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    localStorage.setItem('token', res.data.token);
    return res;
  },

  register: async (email, password) => {
    const res = await api.post('/auth/register', { email, password });
    localStorage.setItem('token', res.data.token);
    return res;
  },

  logout: () => {
    localStorage.removeItem('token');
    window.location.href = '/login';
  },

  isAuthenticated: () => !!localStorage.getItem('token'),
};

// ─── APPOINTMENTS ──────────────────────────────────────────────────────────
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
    api.patch(`/appointments/${id}`, data),

  delete: (id) =>
    api.delete(`/appointments/${id}`),
};

// ─── DOCTORS ───────────────────────────────────────────────────────────────
export const doctorService = {
  getAll: (page = 0, size = 100, specialtyId = null) => {
    let url = `/doctors?page=${page}&size=${size}`;
    if (specialtyId) url += `&specialtyId=${specialtyId}`;
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

  getSchedules: (doctorId, page = 0, size = 20) =>
    api.get(`/doctors/${doctorId}/schedules?page=${page}&size=${size}`),

  createSchedule: (doctorId, data) =>
    api.post(`/doctors/${doctorId}/schedules`, data),

  updateSchedule: (doctorId, id, data) =>
    api.patch(`/doctors/${doctorId}/schedules/${id}`, data),

  deleteSchedule: (doctorId, id) =>
    api.delete(`/doctors/${doctorId}/schedules/${id}`),
};

// ─── PATIENTS ──────────────────────────────────────────────────────────────
export const patientService = {
  getAll: (page = 0, size = 10) =>
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

// ─── OFFICES ───────────────────────────────────────────────────────────────
export const officeService = {
  getAll: (page = 0, size = 10) =>
    api.get(`/offices?page=${page}&size=${size}`),

  create: (data) =>
    api.post('/offices', data),

  update: (id, data) =>
    api.patch(`/offices/${id}`, data),

  delete: (id) =>
    api.delete(`/offices/${id}`),
};

// ─── SPECIALTIES ───────────────────────────────────────────────────────────
export const specialtyService = {
  getAll: (page = 0, size = 100) =>
    api.get(`/specialties?page=${page}&size=${size}`),

  create: (data) =>
    api.post('/specialties', data),

  delete: (id) =>
    api.delete(`/specialties/${id}`),
};

// ─── APPOINTMENT TYPES ─────────────────────────────────────────────────────
export const appointmentTypeService = {
  getAll: (page = 0, size = 100) =>
    api.get(`/appointment-types?page=${page}&size=${size}`),

  create: (data) =>
    api.post('/appointment-types', data),

  delete: (id) =>
    api.delete(`/appointment-types/${id}`),
};

// ─── AVAILABILITY ──────────────────────────────────────────────────────────
export const availabilityService = {
  getAvailableSlots: (doctorId, date, appointmentTypeId, page = 0, size = 50) =>
    api.get(`/availability/doctors/${doctorId}?date=${date}&appointmentTypeId=${appointmentTypeId}&page=${page}&size=${size}`),
};

// ─── REPORTS ───────────────────────────────────────────────────────────────
export const reportService = {
  getOfficeOccupancy: (from, to, page = 0, size = 100) =>
    api.get(`/reports/office-occupancy?from=${from}&to=${to}&page=${page}&size=${size}`),

  getDoctorProductivity: (from, to, page = 0, size = 100) =>
    api.get(`/reports/doctor-productivity?from=${from}&to=${to}&page=${page}&size=${size}`),

  getNoShowPatients: (from, to, page = 0, size = 100) =>
    api.get(`/reports/no-show-patients?from=${from}&to=${to}&page=${page}&size=${size}`),
};

export default api;