// src/services/api.js
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' }
});

// ========== APPOINTMENTS ==========
export const appointmentService = {
  getAll: (page = 0, size = 10, status = null, doctorId = null) => {
    let url = `/appointments?page=${page}&size=${size}`;
    if (status) url += `&status=${status}`;
    if (doctorId) url += `&doctorId=${doctorId}`;
    return api.get(url);
  },
  
  getById: (id) => api.get(`/appointments/${id}`),
  
  create: (data) => api.post('/appointments', data),
  
  confirm: (id) => api.put(`/appointments/${id}/confirm`),
  
  cancel: (id, cancellationReason) => api.put(`/appointments/${id}/cancel`, { cancellationReason }),
  
  complete: (id, observations) => api.put(`/appointments/${id}/complete`, observations ? { observations } : {}),
  
  markNoShow: (id) => api.put(`/appointments/${id}/no-show`),
  
  delete: (id) => api.delete(`/appointments/${id}`)
};

// ========== DOCTORS ==========
export const doctorService = {
  getAll: (page = 0, size = 100, specialtyId = null) => {
    let url = `/doctors?page=${page}&size=${size}`;
    if (specialtyId) url += `&specialtyId=${specialtyId}`;
    return api.get(url);
  },
  
  getById: (id) => api.get(`/doctors/${id}`),
  
  getSchedules: (doctorId, page = 0, size = 10) => 
    api.get(`/doctors/${doctorId}/schedules?page=${page}&size=${size}`),
  
  create: (data) => api.post('/doctors', data),
  
  update: (id, data) => api.patch(`/doctors/${id}`, data),
  
  delete: (id) => api.delete(`/doctors/${id}`)
};

// ========== DOCTOR SCHEDULES ==========
export const doctorScheduleService = {
  create: (doctorId, data) => api.post(`/doctors/${doctorId}/schedules`, data),
  update: (doctorId, id, data) => api.patch(`/doctors/${doctorId}/schedules/${id}`, data),
  delete: (doctorId, id) => api.delete(`/doctors/${doctorId}/schedules/${id}`)
};

// ========== PATIENTS ==========
export const patientService = {
  getAll: (page = 0, size = 10) => api.get(`/patients?page=${page}&size=${size}`),
  getById: (id) => api.get(`/patients/${id}`),
  create: (data) => api.post('/patients', data),
  update: (id, data) => api.patch(`/patients/${id}`, data),
  delete: (id) => api.delete(`/patients/${id}`)
};

// ========== OFFICES ==========
export const officeService = {
  getAll: (page = 0, size = 10) => api.get(`/offices?page=${page}&size=${size}`),
  create: (data) => api.post('/offices', data),
  update: (id, data) => api.patch(`/offices/${id}`, data),
  delete: (id) => api.delete(`/offices/${id}`)
};

// ========== SPECIALTIES ==========
export const specialtyService = {
  getAll: (page = 0, size = 100) => api.get(`/specialties?page=${page}&size=${size}`),
  create: (data) => api.post('/specialties', data),
  delete: (id) => api.delete(`/specialties/${id}`)
};

// ========== APPOINTMENT TYPES ==========
export const appointmentTypeService = {
  getAll: (page = 0, size = 100) => api.get(`/appointment-types?page=${page}&size=${size}`),
  create: (data) => api.post('/appointment-types', data),
  delete: (id) => api.delete(`/appointment-types/${id}`)
};

// ========== AVAILABILITY ==========
export const availabilityService = {
  getAvailableSlots: (doctorId, date, appointmentTypeId, page = 0, size = 20) => 
    api.get(`/availability/doctors/${doctorId}?date=${date}&appointmentTypeId=${appointmentTypeId}&page=${page}&size=${size}`)
};

// ========== REPORTS ==========
export const reportService = {
  getOfficeOccupancy: (from, to, page = 0, size = 10) => 
    api.get(`/reports/office-occupancy?from=${from}&to=${to}&page=${page}&size=${size}`),
  
  getDoctorProductivity: (from, to, page = 0, size = 10) => 
    api.get(`/reports/doctor-productivity?from=${from}&to=${to}&page=${page}&size=${size}`),
  
  getNoShowPatients: (from, to, page = 0, size = 10) => 
    api.get(`/reports/no-show-patients?from=${from}&to=${to}&page=${page}&size=${size}`)
};

export default api;