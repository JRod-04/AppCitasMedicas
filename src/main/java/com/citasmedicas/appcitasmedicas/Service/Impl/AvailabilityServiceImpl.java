package com.citasmedicas.appcitasmedicas.Service.Impl;



import com.citasmedicas.appcitasmedicas.Entity.Appointment;
import com.citasmedicas.appcitasmedicas.Entity.AppointmentType;
import com.citasmedicas.appcitasmedicas.Entity.DoctorSchedule;
import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentRepository;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentTypeRepository;
import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.DoctorScheduleRepository;
import com.citasmedicas.appcitasmedicas.Service.AvailabilityService;
import com.citasmedicas.appcitasmedicas.dto.Response.AvailabilitySlotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AvailabilitySlotResponse> getAvailableSlots(
            Long doctorId,
            LocalDate date,
            Long appointmentTypeId,
            Pageable pageable) {

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }

        AppointmentType appointmentType = appointmentTypeRepository.findById(appointmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found: " + appointmentTypeId));

        List<DoctorSchedule> schedules = doctorScheduleRepository
                .findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek());

        if (schedules.isEmpty()) {
            return Page.empty(pageable);
        }

        // Obtener citas ya confirmadas o programadas del doctor en esa fecha
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        List<Appointment> existing = appointmentRepository
                .findActiveDoctorAppointmentsInRange(doctorId, dayStart, dayEnd, AppointmentStatus.SCHEDULED );

        int duration = appointmentType.getDurationMinutes();
        List<AvailabilitySlotResponse> allSlots = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {
            LocalDateTime cursor = date.atTime(schedule.getStartTime());
            LocalDateTime scheduleEnd = date.atTime(schedule.getEndTime());

            while (!cursor.plusMinutes(duration).isAfter(scheduleEnd)) {
                LocalDateTime slotEnd = cursor.plusMinutes(duration);
                final LocalDateTime slotStart = cursor;

                boolean overlaps = existing.stream().anyMatch(a ->
                        slotStart.isBefore(a.getEndAt()) && slotEnd.isAfter(a.getStartAt())
                );

                if (!overlaps) {
                    allSlots.add(new AvailabilitySlotResponse(slotStart, slotEnd));
                }

                cursor = cursor.plusMinutes(duration);
            }
        }

        // Aplicar paginación
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allSlots.size());

        List<AvailabilitySlotResponse> pagedSlots = allSlots.subList(start, end);

        return new PageImpl<>(pagedSlots, pageable, allSlots.size());
    }
}
