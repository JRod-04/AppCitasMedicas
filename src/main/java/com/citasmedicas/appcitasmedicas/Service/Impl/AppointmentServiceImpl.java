package com.citasmedicas.appcitasmedicas.Service.Impl;

import com.citasmedicas.appcitasmedicas.Entity.*;
import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import com.citasmedicas.appcitasmedicas.Exception.BusinessException;
import com.citasmedicas.appcitasmedicas.Exception.ConflictException;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.*;
import com.citasmedicas.appcitasmedicas.Service.AppointmentService;
import com.citasmedicas.appcitasmedicas.dto.Request.CancelAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentResponse;
import com.citasmedicas.appcitasmedicas.mapper.AppointmentMapper;
import com.citasmedicas.appcitasmedicas.mapper.AppointmentRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final PatientRepository patientRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentRequestMapper appointmentRequestMapper; // ← AGREGAR ESTO


    @Override
    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found: " + request.patientId()));

        if (patient.getStatus() == PatientStatus.INACTIVE) {
            throw new BusinessException(
                    "Patient is inactive and cannot book appointments");
        }

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found: " + request.doctorId()));

        if (!doctor.isActive()) {
            throw new BusinessException(
                    "Doctor is inactive and cannot receive appointments");
        }

        Office office = officeRepository.findById(request.officeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Office not found: " + request.officeId()));

        if (office.getStatus() != OfficeStatus.ACTIVE) {
            throw new BusinessException("Office is not active");
        }

        AppointmentType appointmentType =
                appointmentTypeRepository.findById(request.appointmentTypeId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Appointment type not found: "
                                        + request.appointmentTypeId()));

        LocalDateTime startAt = request.startAt();

        if (startAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Cannot create an appointment in the past");
        }

        LocalDateTime endAt = startAt.plusMinutes(appointmentType.getDurationMinutes());

        DayOfWeek dayOfWeek = startAt.getDayOfWeek();

        List<DoctorSchedule> schedules =
                doctorScheduleRepository.findByDoctorIdAndDayOfWeek(
                        doctor.getId(),
                        dayOfWeek
                );

        boolean withinSchedule = schedules.stream().anyMatch(schedule ->
                !startAt.toLocalTime().isBefore(schedule.getStartTime())
                        && !endAt.toLocalTime().isAfter(schedule.getEndTime()));

        if (!withinSchedule) {
            throw new BusinessException(
                    "Appointment is outside the doctor's working hours");
        }

        if (appointmentRepository.existsDoctorOverlap(
                doctor.getId(),
                startAt,
                endAt,
                null,
                AppointmentStatus.CANCELLED)) {

            throw new ConflictException(
                    "Doctor already has an appointment in that time range");
        }

        if (appointmentRepository.existsOfficeOverlap(
                office.getId(),
                startAt,
                endAt,
                null,
                AppointmentStatus.CANCELLED)) {

            throw new ConflictException(
                    "Office is already occupied in that time range");
        }

        if (appointmentRepository.existsPatientOverlap(
                patient.getId(),
                startAt,
                endAt,
                null,
                AppointmentStatus.CANCELLED)) {

            throw new ConflictException(
                    "Patient already has an appointment in that time range");
        }

        // USAR EL MAPPER PARA CREAR LA ENTIDAD
        Appointment appointment = appointmentRequestMapper.toEntity(request);

        appointment = appointmentRepository.save(appointment);

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        return appointmentMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAll(Pageable page) {
        return appointmentRepository.findAll(page)
                .map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse confirm(Long id) {

        Appointment appointment = getOrThrow(id);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException(
                    "Only SCHEDULED appointments can be confirmed. Current status: "
                            + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());

        return appointmentMapper.toResponse(
                appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse cancel(
            Long id,
            CancelAppointmentRequest request) {

        Appointment appointment = getOrThrow(id);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException(
                    "Cannot cancel a COMPLETED appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessException(
                    "Cannot cancel a NO_SHOW appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException(
                    "Appointment is already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(request.cancellationReason());
        appointment.setUpdatedAt(LocalDateTime.now());

        appointment = appointmentRepository.save(appointment);

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse complete(Long id, String observations) {

        Appointment appointment = getOrThrow(id);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException(
                    "Only CONFIRMED appointments can be completed. Current status: "
                            + appointment.getStatus());
        }

        if (LocalDateTime.now().isBefore(appointment.getStartAt())) {
            throw new BusinessException(
                    "Cannot complete an appointment before its scheduled start time");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setObservations(observations);
        appointment.setUpdatedAt(LocalDateTime.now());

        appointment = appointmentRepository.save(appointment);

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse markNoShow(Long id) {

        Appointment appointment = getOrThrow(id);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException(
                    "Only CONFIRMED appointments can be marked as NO_SHOW. Current status: "
                            + appointment.getStatus());
        }

        if (LocalDateTime.now().isBefore(appointment.getStartAt())) {
            throw new BusinessException(
                    "Cannot mark an appointment as NO_SHOW before its scheduled start time");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment.setUpdatedAt(LocalDateTime.now());

        appointment = appointmentRepository.save(appointment);

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Appointment appointment = getOrThrow(id);
        appointmentRepository.delete(appointment);
    }

    private Appointment getOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with id: " + id));
    }
    @Override
@Transactional
public AppointmentResponse update(Long id, UpdateAppointmentRequest request) {
    // 1. Buscar la cita existente
    Appointment appointment = getOrThrow(id);
    
    // 2. Solo se pueden actualizar citas AGENDADAS o CONFIRMADAS
    if (appointment.getStatus() != AppointmentStatus.SCHEDULED 
            && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
        throw new BusinessException(
            "No se puede actualizar la cita con estado: " + appointment.getStatus() + 
            ". Solo las citas AGENDADAS o CONFIRMADAS pueden ser actualizadas."
        );
    }
    
    // 3. Verificar si se va a cambiar el consultorio
    if (request.officeId() != null) {
        Office newOffice = officeRepository.findById(request.officeId())
                .orElseThrow(() -> new ResourceNotFoundException("Consultorio no encontrado con ID: " + request.officeId()));
        
        if (newOffice.getStatus() != OfficeStatus.ACTIVE) {
            throw new BusinessException("El consultorio no está activo");
        }
        appointment.setOffice(newOffice);
    }
    
    // 4. Verificar si se va a cambiar la fecha/hora
    LocalDateTime startAt = request.startAt() != null ? request.startAt() : appointment.getStartAt();
    LocalDateTime endAt = request.endAt() != null ? request.endAt() : appointment.getEndAt();
    
    if (request.startAt() != null || request.endAt() != null) {
        // Validar que endAt sea después de startAt
        if (endAt.isBefore(startAt) || endAt.equals(startAt)) {
            throw new BusinessException("La hora de finalización debe ser posterior a la hora de inicio");
        }
        
        // Validar que no sea en el pasado
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException("No se puede agendar una cita en el pasado");
        }
        
        // Validar horario del doctor
        Doctor doctor = appointment.getDoctor();
        DayOfWeek dayOfWeek = startAt.getDayOfWeek();
        
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(
                doctor.getId(), dayOfWeek);
        
        boolean withinSchedule = schedules.stream().anyMatch(schedule ->
                !startAt.toLocalTime().isBefore(schedule.getStartTime())
                        && !endAt.toLocalTime().isAfter(schedule.getEndTime()));
        
        if (!withinSchedule) {
            throw new BusinessException("El nuevo horario está fuera del horario laboral del médico");
        }
        
        // Validar superposiciones (excluyendo la cita actual)
        if (appointmentRepository.existsDoctorOverlap(
                doctor.getId(), startAt, endAt, id, AppointmentStatus.CANCELLED)) {
            throw new ConflictException("El médico ya tiene una cita agendada en ese horario");
        }
        
        if (appointmentRepository.existsOfficeOverlap(
                appointment.getOffice().getId(), startAt, endAt, id, AppointmentStatus.CANCELLED)) {
            throw new ConflictException("El consultorio ya está ocupado en ese horario");
        }
        
        if (appointmentRepository.existsPatientOverlap(
                appointment.getPatient().getId(), startAt, endAt, id, AppointmentStatus.CANCELLED)) {
            throw new ConflictException("El paciente ya tiene una cita agendada en ese horario");
        }
        
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
    }
    
    // 5. Actualizar observaciones si se enviaron
    if (request.observations() != null) {
        appointment.setObservations(request.observations());
    }
    
    appointment.setUpdatedAt(LocalDateTime.now());
    
    return appointmentMapper.toResponse(appointmentRepository.save(appointment));
}
    
}