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
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentResponse;
import com.citasmedicas.appcitasmedicas.mapper.AppointmentMapper;
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
        // Configurar datos comunes
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
                1L, 1L, 1L, 1L,           // patientId, doctorId, officeId, appointmentTypeId
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),  // startAt
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(30), // endAt
                null                        // observations (puede ser null)
        );

        cancelRequest = new CancelAppointmentRequest("Paciente no pudo asistir");
    }

    // ==================== CREATE TESTS ====================

    @Test
    @DisplayName("create - debe crear una cita exitosamente")
    void shouldCreateAppointmentSuccessfully() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.of(appointmentType));

        LocalDateTime startAt = createRequest.startAt();
        LocalDateTime endAt = startAt.plusMinutes(30);

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

        // When
        AppointmentResponse result = appointmentService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el paciente no existe")
    void shouldThrowWhenPatientNotFound() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el paciente está inactivo")
    void shouldThrowWhenPatientIsInactive() {
        // Given
        patient.setStatus(PatientStatus.INACTIVE);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Patient is inactive");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el doctor no existe")
    void shouldThrowWhenDoctorNotFound() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el doctor está inactivo")
    void shouldThrowWhenDoctorIsInactive() {
        // Given
        doctor.setActive(false);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Doctor is inactive");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el consultorio no existe")
    void shouldThrowWhenOfficeNotFound() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Office not found: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el consultorio no está activo")
    void shouldThrowWhenOfficeIsNotActive() {
        // Given
        office.setStatus(OfficeStatus.INACTIVE);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Office is not active");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el tipo de cita no existe")
    void shouldThrowWhenAppointmentTypeNotFound() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment type not found: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando la fecha es en el pasado")
    void shouldThrowWhenStartDateIsInPast() {
        // Given
        var pastRequest = new CreateAppointmentRequest(
                1L, 1L, 1L, 1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1).plusMinutes(30),
                "Cita de señora de la tercera edad"
        );
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(1L)).thenReturn(Optional.of(appointmentType));

        // When & Then
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

        // Horario: 9:00 a 17:00
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(List.of(DoctorSchedule.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()));

        // ✅ Cita a las 18:00 (FUERA del horario)
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
        // Given
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

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Doctor already has an appointment");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando hay superposición con consultorio")
    void shouldThrowWhenOfficeOverlap() {
        // Given
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

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Office is already occupied");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando hay superposición con paciente")
    void shouldThrowWhenPatientOverlap() {
        // Given
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

        // When & Then
        assertThatThrownBy(() -> appointmentService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Patient already has an appointment");
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    @DisplayName("findById - debe encontrar una cita por ID")
    void shouldFindById() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

        // When
        AppointmentResponse result = appointmentService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - debe lanzar excepción cuando la cita no existe")
    void shouldThrowWhenAppointmentNotFound() {
        // Given
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found with id: 999");
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    @DisplayName("findAll - debe retornar página de citas")
    void shouldFindAll() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
        when(appointmentRepository.findAll(pageable)).thenReturn(appointmentPage);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        Page<AppointmentResponse> result = appointmentService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    // ==================== CONFIRM TESTS ====================

    @Test
    @DisplayName("confirm - debe confirmar una cita exitosamente")
    void shouldConfirmAppointment() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        AppointmentResponse result = appointmentService.confirm(1L);

        // Then
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("confirm - debe lanzar excepción cuando la cita no está SCHEDULED")
    void shouldThrowWhenConfirmingNonScheduledAppointment() {
        // Given
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.confirm(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only SCHEDULED appointments can be confirmed");
    }

    // ==================== CANCEL TESTS ====================

    @Test
    @DisplayName("cancel - debe cancelar una cita exitosamente")
    void shouldCancelAppointment() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        AppointmentResponse result = appointmentService.cancel(1L, cancelRequest);

        // Then
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("cancel - debe lanzar excepción cuando la cita ya está COMPLETED")
    void shouldThrowWhenCancellingCompletedAppointment() {
        // Given
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.cancel(1L, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel a COMPLETED appointment");
    }

    @Test
    @DisplayName("cancel - debe lanzar excepción cuando la cita ya está NO_SHOW")
    void shouldThrowWhenCancellingNoShowAppointment() {
        // Given
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.cancel(1L, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel a NO_SHOW appointment");
    }

    @Test
    @DisplayName("cancel - debe lanzar excepción cuando la cita ya está CANCELLED")
    void shouldThrowWhenCancellingAlreadyCancelledAppointment() {
        // Given
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.cancel(1L, cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Appointment is already cancelled");
    }

    // ==================== COMPLETE TESTS ====================

    @Test
    @DisplayName("complete - debe completar una cita exitosamente")
    void shouldCompleteAppointment() {
        // Given
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        AppointmentResponse result = appointmentService.complete(1L, "Paciente atendido correctamente");

        // Then
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("complete - debe lanzar excepción cuando la cita no está CONFIRMED")
    void shouldThrowWhenCompletingNonConfirmedAppointment() {
        // Given
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.complete(1L, "Observaciones"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CONFIRMED appointments can be completed");
    }

    @Test
    @DisplayName("complete - debe lanzar excepción cuando se completa antes de la hora")
    void shouldThrowWhenCompletingBeforeStartTime() {
        // Given
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().plusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.complete(1L, "Observaciones"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot complete an appointment before its scheduled start time");
    }

    // ==================== MARK NO SHOW TESTS ====================

    @Test
    @DisplayName("markNoShow - debe marcar una cita como NO_SHOW exitosamente")
    void shouldMarkNoShow() {
        // Given
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        AppointmentResponse result = appointmentService.markNoShow(1L);

        // Then
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("markNoShow - debe lanzar excepción cuando la cita no está CONFIRMED")
    void shouldThrowWhenMarkingNoShowOnNonConfirmedAppointment() {
        // Given
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.markNoShow(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CONFIRMED appointments can be marked as NO_SHOW");
    }

    @Test
    @DisplayName("markNoShow - debe lanzar excepción cuando se marca antes de la hora")
    void shouldThrowWhenMarkingNoShowBeforeStartTime() {
        // Given
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().plusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.markNoShow(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot mark an appointment as NO_SHOW before its scheduled start time");
    }
}