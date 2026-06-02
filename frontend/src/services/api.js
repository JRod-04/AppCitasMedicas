// src/services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',                               // usa el proxy de package.json
  headers: { 'Content-Type': 'application/json' }
});

// Interceptor global: loguea errores sin romper el flujo
api.interceptors.response.use(
  res => res,
  err => {
    console.error('[API Error]', err.response?.status, err.config?.url, err.response?.data);
    return Promise.reject(err);
  }
);

// ─── HELPERS ──────────────────────────────────────────────
// El backend devuelve Page<T> para la mayoría de endpoints
// (con .content, .totalPages, .totalElements).
// DoctorController.findAll devuelve List<T> directamente.
// Usamos estos helpers para normalizar en un solo lugar.

/** Extrae el array de un objeto Page o de un List plano */
export const getContent  = (data) => Array.isArray(data) ? data : (data?.content ?? []);
export const getTotal    = (data) => data?.totalElements ?? (Array.isArray(data) ? data.length : 0);
export const getTotalPgs = (data) => data?.totalPages    ?? 1;

// ─── APPOINTMENTS ──────────────────────────────────────────
// Backend: GET /api/appointments?page=&size=
// No admite filtros status/doctorId (el backend no los tiene implementados)
export const appointmentService = {
  getAll: (page = 0, size = 10) =>
    api.get(`/appointments?page=${page}&size=${size}`),

  getById: (id) =>
    api.get(`/appointments/${id}`),

  create: (data) =>
    api.post('/appointments', data),

  confirm: (id) =>
    api.put(`/appointments/${id}/confirm`),

  // Backend espera { cancellationReason: "..." }
  cancel: (id, cancellationReason) =>
    api.put(`/appointments/${id}/cancel`, { cancellationReason }),

  // Backend acepta body opcional { observations }
  complete: (id, observations = null) =>
    api.put(`/appointments/${id}/complete`, observations ? { observations } : {}),

  markNoShow: (id) =>
    api.put(`/appointments/${id}/no-show`),

  update: (id, data) =>
    api.patch(`/appointments/${id}`, data),

  delete: (id) =>
    api.delete(`/appointments/${id}`),
};

// ─── DOCTORS ───────────────────────────────────────────────
// Backend: GET /api/doctors → devuelve List<DoctorResponse> (NO Page)
// Por eso usamos getContent que maneja ambos casos.
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

  // Horarios del doctor
  getSchedules: (doctorId, page = 0, size = 20) =>
    api.get(`/doctors/${doctorId}/schedules?page=${page}&size=${size}`),

  createSchedule: (doctorId, data) =>
    api.post(`/doctors/${doctorId}/schedules`, data),

  // Backend: PATCH /api/doctors/{doctorId}/schedules/{id}
  updateSchedule: (doctorId, id, data) =>
    api.patch(`/doctors/${doctorId}/schedules/${id}`, data),

  deleteSchedule: (doctorId, id) =>
    api.delete(`/doctors/${doctorId}/schedules/${id}`),
};

// ─── PATIENTS ──────────────────────────────────────────────
// Backend: GET /api/patients → Page<PatientResponse>
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

// ─── OFFICES ───────────────────────────────────────────────
// Backend: GET /api/offices → Page<OfficeResponse>
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

// ─── SPECIALTIES ───────────────────────────────────────────
// Backend: GET /api/specialties → Page<SpecialtyResponse>
export const specialtyService = {
  getAll: (page = 0, size = 100) =>
    api.get(`/specialties?page=${page}&size=${size}`),

  create: (data) =>
    api.post('/specialties', data),

  delete: (id) =>
    api.delete(`/specialties/${id}`),
};

// ─── APPOINTMENT TYPES ─────────────────────────────────────
// Backend: GET /api/appointment-types → Page<AppointmentTypeResponse>
export const appointmentTypeService = {
  getAll: (page = 0, size = 100) =>
    api.get(`/appointment-types?page=${page}&size=${size}`),

  create: (data) =>
    api.post('/appointment-types', data),

  delete: (id) =>
    api.delete(`/appointment-types/${id}`),
};

// ─── AVAILABILITY ──────────────────────────────────────────
// Backend: GET /api/availability/doctors/{doctorId}?date=&appointmentTypeId=&page=&size=
export const availabilityService = {
  getAvailableSlots: (doctorId, date, appointmentTypeId, page = 0, size = 50) =>
    api.get(`/availability/doctors/${doctorId}?date=${date}&appointmentTypeId=${appointmentTypeId}&page=${page}&size=${size}`),
};

// ─── REPORTS ───────────────────────────────────────────────
// Backend espera fechas en formato ISO: YYYY-MM-DD
export const reportService = {
  getOfficeOccupancy: (from, to, page = 0, size = 100) =>
    api.get(`/reports/office-occupancy?from=${from}&to=${to}&page=${page}&size=${size}`),

  getDoctorProductivity: (from, to, page = 0, size = 100) =>
    api.get(`/reports/doctor-productivity?from=${from}&to=${to}&page=${page}&size=${size}`),

  getNoShowPatients: (from, to, page = 0, size = 100) =>
    api.get(`/reports/no-show-patients?from=${from}&to=${to}&page=${page}&size=${size}`),
};

export default api;