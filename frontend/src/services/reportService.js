import api from './api';

export const reportService = {
  getOfficeOccupancy: (from, to, page = 0, size = 10) =>
    api.get(`/reports/office-occupancy?from=${from}&to=${to}&page=${page}&size=${size}`),

  getDoctorProductivity: (from, to, page = 0, size = 10) =>
    api.get(`/reports/doctor-productivity?from=${from}&to=${to}&page=${page}&size=${size}`),

  getNoShowPatients: (from, to, page = 0, size = 10) =>
    api.get(`/reports/no-show-patients?from=${from}&to=${to}&page=${page}&size=${size}`),
};