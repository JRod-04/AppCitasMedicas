package com.citasmedicas.appcitasmedicas.ServiceImplTests;

import com.citasmedicas.appcitasmedicas.Entity.*;
import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import com.citasmedicas.appcitasmedicas.Exception.BusinessException;
import com.citasmedicas.appcitasmedicas.Exception.ConflictException;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.*;
import com.citasmedicas.appcitasmedicas.Service.Impl.AppointmentServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Request.CancelAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentResponse;
import com.citasmedicas.appcitasmedicas.mapper.AppointmentMapper;
import com.citasmedicas.appcitasmedicas.mapper.AppointmentRequestMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Tests")
class AppointmentServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentTypeRepository appointmentTypeRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private AppointmentRequestMapper appointmentRequestMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Office office;
    private AppointmentType appointmentType;
    private Appointment appointment;
    private AppointmentResponse appointmentResponse;
    private CreateAppointmentRequest createRequest;
    private CancelAppointmentRequest cancelRequest;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .firstName("Ana")
                .lastName("Torres")
                .documentNumber("11111111")
                .email("ana@test.com")
                .phone("3001234567")
                .status(PatientStatus.ACTIVE)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .firstName("Pedro")
                .lastName("Gil")
                .licenseNumber("LIC-100")
                .email("pedro@test.com")
                .active(true)
                .build();

        office = Office.builder()
                .id(1L)
                .name("Consultorio 101")
                .location("Piso 1")
                .floor("101")
                .status(OfficeStatus.ACTIVE)
                .build();

        appointmentType = AppointmentType.builder()
                .id(1L)
                .name("Consulta General")
                .durationMinutes(30)
                .description("Consulta estándar")
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .endAt(LocalDateTime.now().plusDays(1).withHour(10).withMinute(30))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        appointmentResponse = AppointmentResponse.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(1L)
                .officeId(1L)
                .appointmentTypeId(1L)
                .startAt(appointment.getStartAt())
                .endAt(appointment.getEndAt())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        createRequest = new CreateAppointmentRequest(
                1L, 1L, 1L, 1L,
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(30),
                null
        );

        cancelRequest = new CancelAppointmentRequest("Paciente no pudo asistir");
    }

    // ==================== CREATE TESTS ====================

    @Test
    @DisplayName("create - debe crear una cita exitosamente")
    void shouldCreateAppointmentSuccessfully() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.of(appointmentType));

        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));

        when(appointmentRepository.existsDoctorOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsOfficeOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsPatientOverlap(any(), any(), any(), any(), any())).thenReturn(false);

        when(appointmentRequestMapper.toEntity(any(CreateAppointmentRequest.class))).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        AppointmentResponse result = appointmentService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el paciente no existe")
    void shouldThrowWhenPatientNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el paciente está inactivo")
    void shouldThrowWhenPatientIsInactive() {
        patient.setStatus(PatientStatus.INACTIVE);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Patient is inactive");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el doctor no existe")
    void shouldThrowWhenDoctorNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el doctor está inactivo")
    void shouldThrowWhenDoctorIsInactive() {
        doctor.setActive(false);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Doctor is inactive");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el consultorio no existe")
    void shouldThrowWhenOfficeNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Office not found: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el consultorio no está activo")
    void shouldThrowWhenOfficeIsNotActive() {
        office.setStatus(OfficeStatus.INACTIVE);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Office is not active");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el tipo de cita no existe")
    void shouldThrowWhenAppointmentTypeNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment type not found: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando la fecha es en el pasado")
    void shouldThrowWhenStartDateIsInPast() {
        var pastRequest = new CreateAppointmentRequest(
                1L, 1L, 1L, 1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1).plusMinutes(30),
                null
        );
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.of(appointmentType));

        assertThatThrownBy(() -> appointmentService.create(pastRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot create an appointment in the past");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando está fuera del horario laboral")
    void shouldThrowWhenOutsideWorkingHours() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.of(appointmentType));

        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));

        var outOfHoursRequest = new CreateAppointmentRequest(
                1L, 1L, 1L, 1L,
                LocalDateTime.now().plusDays(1).withHour(18).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(18).withMinute(30),
                null
        );

        assertThatThrownBy(() -> appointmentService.create(outOfHoursRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("outside the doctor's working hours");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando hay superposición con doctor")
    void shouldThrowWhenDoctorOverlap() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));
        when(appointmentRepository.existsDoctorOverlap(any(), any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Doctor already has an appointment");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando hay superposición con consultorio")
    void shouldThrowWhenOfficeOverlap() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));
        when(appointmentRepository.existsDoctorOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsOfficeOverlap(any(), any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Office is already occupied");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando hay superposición con paciente")
    void shouldThrowWhenPatientOverlap() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));
        when(appointmentRepository.existsDoctorOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsOfficeOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsPatientOverlap(any(), any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Patient already has an appointment");
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    @DisplayName("findById - debe encontrar una cita por ID")
    void shouldFindById() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

        AppointmentResponse result = appointmentService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - debe lanzar excepción cuando la cita no existe")
    void shouldThrowWhenAppointmentNotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found with id: 999");
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    @DisplayName("findAll - debe retornar página de citas")
    void shouldFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
        when(appointmentRepository.findAll(pageable)).thenReturn(appointmentPage);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        Page<AppointmentResponse> result = appointmentService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    // ==================== CONFIRM TESTS ====================

    @Test
    @DisplayName("confirm - debe confirmar una cita exitosamente")
    void shouldConfirmAppointment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        AppointmentResponse result = appointmentService.confirm(1L);

        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("confirm - debe lanzar excepción cuando la cita no está SCHEDULED")
    void shouldThrowWhenConfirmingNonScheduledAppointment() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.confirm(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only SCHEDULED appointments can be confirmed");
    }

    // ==================== CANCEL TESTS ====================

    @Test
    @DisplayName("cancel - debe cancelar una cita exitosamente")
    void shouldCancelAppointment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        AppointmentResponse result = appointmentService.cancel(1L, cancelRequest);

        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("cancel - debe lanzar excepción cuando la cita ya está COMPLETED")
    void shouldThrowWhenCancellingCompletedAppointment() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(1L, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel a COMPLETED appointment");
    }

    @Test
    @DisplayName("cancel - debe lanzar excepción cuando la cita ya está NO_SHOW")
    void shouldThrowWhenCancellingNoShowAppointment() {
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(1L, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel a NO_SHOW appointment");
    }

    @Test
    @DisplayName("cancel - debe lanzar excepción cuando la cita ya está CANCELLED")
    void shouldThrowWhenCancellingAlreadyCancelledAppointment() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(1L, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Appointment is already cancelled");
    }

    // ==================== COMPLETE TESTS ====================

    @Test
    @DisplayName("complete - debe completar una cita exitosamente")
    void shouldCompleteAppointment() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        AppointmentResponse result = appointmentService.complete(1L, "Paciente atendido correctamente");

        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("complete - debe lanzar excepción cuando la cita no está CONFIRMED")
    void shouldThrowWhenCompletingNonConfirmedAppointment() {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.complete(1L, "Observaciones"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CONFIRMED appointments can be completed");
    }

    @Test
    @DisplayName("complete - debe lanzar excepción cuando se completa antes de la hora")
    void shouldThrowWhenCompletingBeforeStartTime() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().plusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.complete(1L, "Observaciones"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot complete an appointment before its scheduled start time");
    }

    // ==================== MARK NO SHOW TESTS ====================

    @Test
    @DisplayName("markNoShow - debe marcar una cita como NO_SHOW exitosamente")
    void shouldMarkNoShow() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        AppointmentResponse result = appointmentService.markNoShow(1L);

        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("markNoShow - debe lanzar excepción cuando la cita no está CONFIRMED")
    void shouldThrowWhenMarkingNoShowOnNonConfirmedAppointment() {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.markNoShow(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CONFIRMED appointments can be marked as NO_SHOW");
    }

    @Test
    @DisplayName("markNoShow - debe lanzar excepción cuando se marca antes de la hora")
    void shouldThrowWhenMarkingNoShowBeforeStartTime() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().plusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.markNoShow(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot mark an appointment as NO_SHOW before its scheduled start time");
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("update - debe actualizar la fecha de una cita exitosamente")
    void shouldUpdateAppointmentDateSuccessfully() {
        LocalDateTime newStartAt = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
        LocalDateTime newEndAt = newStartAt.plusMinutes(30);
        
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                null, newStartAt, newEndAt, null
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));
        when(appointmentRepository.existsDoctorOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsOfficeOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsPatientOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);
        
        AppointmentResponse result = appointmentService.update(1L, updateRequest);
        
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("update - debe actualizar el consultorio de una cita exitosamente")
    void shouldUpdateAppointmentOfficeSuccessfully() {
        Office newOffice = Office.builder()
                .id(2L)
                .name("Consultorio 202")
                .location("Piso 2")
                .floor("202")
                .status(OfficeStatus.ACTIVE)
                .build();
        
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                2L, null, null, null
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(officeRepository.findById(2L)).thenReturn(Optional.of(newOffice));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);
        
        AppointmentResponse result = appointmentService.update(1L, updateRequest);
        
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("update - debe actualizar observaciones de una cita exitosamente")
    void shouldUpdateAppointmentObservationsSuccessfully() {
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                null, null, null, "Observación actualizada"
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);
        
        AppointmentResponse result = appointmentService.update(1L, updateRequest);
        
        assertThat(result).isNotNull();
        assertThat(appointment.getObservations()).isEqualTo("Observación actualizada");
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("update - debe lanzar excepción cuando la cita está COMPLETED")
    void shouldThrowWhenUpdatingCompletedAppointment() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                null, null, null, "Nueva observación"
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        
        assertThatThrownBy(() -> appointmentService.update(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede actualizar la cita con estado: COMPLETED");
    }

    @Test
    @DisplayName("update - debe lanzar excepción cuando la cita está CANCELLED")
    void shouldThrowWhenUpdatingCancelledAppointment() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                null, null, null, "Nueva observación"
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        
        assertThatThrownBy(() -> appointmentService.update(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede actualizar la cita con estado: CANCELLED");
    }

    @Test
    @DisplayName("update - debe lanzar excepción cuando el nuevo consultorio no existe")
    void shouldThrowWhenNewOfficeNotFound() {
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                999L, null, null, null
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(officeRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> appointmentService.update(1L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Consultorio no encontrado con ID: 999");
    }

    @Test
    @DisplayName("update - debe lanzar excepción cuando la nueva fecha está fuera del horario")
    void shouldThrowWhenNewDateOutsideWorkingHours() {
        LocalDateTime newStartAt = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0);
        LocalDateTime newEndAt = newStartAt.plusMinutes(30);
        
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                null, newStartAt, newEndAt, null
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));
        
        assertThatThrownBy(() -> appointmentService.update(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El nuevo horario está fuera del horario laboral del médico");
    }

    @Test
    @DisplayName("update - debe lanzar excepción cuando el consultorio no está activo")
    void shouldThrowWhenNewOfficeIsNotActive() {
        Office newOffice = Office.builder()
                .id(2L)
                .name("Consultorio 202")
                .status(OfficeStatus.INACTIVE)
                .build();
        
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                2L, null, null, null
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(officeRepository.findById(2L)).thenReturn(Optional.of(newOffice));
        
        assertThatThrownBy(() -> appointmentService.update(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El consultorio no está activo");
    }

    @Test
    @DisplayName("update - debe lanzar excepción cuando hay superposición con médico")
    void shouldThrowWhenDoctorOverlapOnUpdate() {
        LocalDateTime newStartAt = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
        LocalDateTime newEndAt = newStartAt.plusMinutes(30);
        
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                null, newStartAt, newEndAt, null
        );
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));
        when(appointmentRepository.existsDoctorOverlap(any(), any(), any(), any(), any())).thenReturn(true);
        
        assertThatThrownBy(() -> appointmentService.update(1L, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("El médico ya tiene una cita agendada en ese horario");
    }
}