// ========== AVAILABILITY ==========
export const availabilityService = {
  getAvailableSlots: (doctorId, date, appointmentTypeId, page = 0, size = 20) => 
    api.get(`/availability/doctors/${doctorId}?date=${date}&appointmentTypeId=${appointmentTypeId}&page=${page}&size=${size}`)
};