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
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentResponse;
import com.citasmedicas.appcitasmedicas.mapper.AppointmentMapper;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final PatientRepository patientRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentServiceImpl(
            DoctorRepository doctorRepository,
            DoctorScheduleRepository doctorScheduleRepository,
            AppointmentRepository appointmentRepository,
            AppointmentTypeRepository appointmentTypeRepository,
            PatientRepository patientRepository,
            OfficeRepository officeRepository,
            AppointmentMapper appointmentMapper) {

        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentTypeRepository = appointmentTypeRepository;
        this.patientRepository = patientRepository;
        this.officeRepository = officeRepository;
        this.appointmentMapper = appointmentMapper;
    }

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

        LocalDateTime endAt =
                startAt.plusMinutes(appointmentType.getDurationMinutes());

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
                AppointmentStatus.CANCELLED)) {  // ✅

            throw new ConflictException(
                    "Patient already has an appointment in that time range");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(startAt)
                .endAt(endAt)
                .status(AppointmentStatus.SCHEDULED)
                .build();

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

    private Appointment getOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with id: " + id));
    }
}
