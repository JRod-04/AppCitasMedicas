package com.citasmedicas.appcitasmedicas.RepositoryTest;


import com.citasmedicas.appcitasmedicas.Entity.*;
import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import com.citasmedicas.appcitasmedicas.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private OfficeRepository officeRepository;
    @Autowired private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired private SpecialtyRepository specialtyRepository;
    @Autowired private DoctorScheduleRepository doctorScheduleRepository;

    private Patient patient;
    private Doctor doctor;
    private Office office;
    private AppointmentType appointmentType;

    @BeforeEach
    void setUp() {
        var specialty = specialtyRepository.save(Specialty.builder()
                .name("Cardiología")
                .build());

        patient = patientRepository.save(Patient.builder()
                .firstName("Ana")
                .lastName("Torres")
                .documentNumber("11111111")
                .email("ana@test.com")
                .phone("3001234567")
                .status(PatientStatus.ACTIVE)
                .build());

        doctor = doctorRepository.save(Doctor.builder()
                .firstName("Pedro")
                .lastName("Gil")
                .licenseNumber("LIC-100")
                .email("pedro@test.com")
                .specialty(specialty)
                .active(true)
                .build());

        office = officeRepository.save(Office.builder()
                .name("Consultorio 101")
                .location("Piso 1")
                .floor("101")
                .status(OfficeStatus.ACTIVE)
                .build());

        appointmentType = appointmentTypeRepository.save(AppointmentType.builder()
                .name("Consulta General")
                .durationMinutes(30)
                .description("Consulta estándar")
                .build());
    }

    private Appointment createAppointment(LocalDateTime start, LocalDateTime end, AppointmentStatus status) {
        return Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(start)
                .endAt(end)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("findByPatientIdAndStatus - encuentra citas por paciente y estado")
    void shouldFindByPatientIdAndStatus() {
        var start = LocalDateTime.now().plusDays(1);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        var result = appointmentRepository.findByPatientIdAndStatus(patient.getId(), AppointmentStatus.SCHEDULED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("findByStartAtBetween - encuentra citas en rango de fechas")
    void shouldFindByStartAtBetween() {
        var base = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        var appointment = createAppointment(base, base.plusMinutes(30), AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        var result = appointmentRepository.findByStartAtBetween(base.minusHours(1), base.plusDays(2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartAt()).isEqualTo(base);
    }

    @Test
    @DisplayName("findByDoctorId - encuentra citas por doctor")
    void shouldFindByDoctorId() {
        var start = LocalDateTime.now().plusDays(1);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        var result = appointmentRepository.findByDoctorId(doctor.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctor().getId()).isEqualTo(doctor.getId());
    }

    @Test
    @DisplayName("findByPatientId - encuentra citas por paciente")
    void shouldFindByPatientId() {
        var start = LocalDateTime.now().plusDays(1);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        var result = appointmentRepository.findByPatientId(patient.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatient().getId()).isEqualTo(patient.getId());
    }

    @Test
    @DisplayName("existsDoctorOverlap - detecta superposición de horario para doctor")
    void shouldDetectDoctorOverlap() {
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        var overlap = appointmentRepository.existsDoctorOverlap(doctor.getId(), start.plusMinutes(15), start.plusMinutes(45), null);

        assertThat(overlap).isTrue();
    }

    @Test
    @DisplayName("existsDoctorOverlap - no detecta superposición cuando no hay conflicto")
    void shouldNotDetectDoctorOverlap() {
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        var overlap = appointmentRepository.existsDoctorOverlap(doctor.getId(), start.plusMinutes(30), start.plusMinutes(60), null);

        assertThat(overlap).isFalse();
    }

    @Test
    @DisplayName("existsDoctorOverlap - ignora la cita actual en actualizaciones")
    void shouldIgnoreCurrentAppointmentWhenCheckingOverlap() {
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED);
        var saved = appointmentRepository.save(appointment);

        var overlap = appointmentRepository.existsDoctorOverlap(doctor.getId(), start.plusMinutes(15), start.plusMinutes(45), saved.getId());

        assertThat(overlap).isFalse();
    }

    @Test
    @DisplayName("existsOfficeOverlap - detecta superposición de horario para consultorio")
    void shouldDetectOfficeOverlap() {
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        var overlap = appointmentRepository.existsOfficeOverlap(office.getId(), start.plusMinutes(10), start.plusMinutes(40), null);

        assertThat(overlap).isTrue();
    }

    @Test
    @DisplayName("existsOfficeOverlap - citas canceladas no bloquean el consultorio")
    void shouldNotBlockOfficeWhenAppointmentIsCancelled() {
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        var overlap = appointmentRepository.existsOfficeOverlap(office.getId(), start, start.plusMinutes(30), null);

        assertThat(overlap).isFalse();
    }

    @Test
    @DisplayName("existsPatientOverlap - detecta superposición de horario para paciente")
    void shouldDetectPatientOverlap() {
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        var overlap = appointmentRepository.existsPatientOverlap(patient.getId(), start.plusMinutes(5), start.plusMinutes(35), null);

        assertThat(overlap).isTrue();
    }

    @Test
    @DisplayName("findActiveDoctorAppointmentsInRange - encuentra citas activas en rango")
    void shouldFindActiveDoctorAppointmentsInRange() {
        var from = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        var to = from.plusDays(1);
        var start = from.withHour(10).withMinute(0);
        var appointment = createAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        var result = appointmentRepository.findActiveDoctorAppointmentsInRange(doctor.getId(), from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctor().getId()).isEqualTo(doctor.getId());
    }

    @Test
    @DisplayName("findOfficeOccupancy - agrupa ocupación por consultorio")
    void shouldFindOfficeOccupancy() {
        var from = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        var to = from.plusDays(1);
        var start = from.withHour(9).withMinute(0);

        appointmentRepository.save(createAppointment(start, start.plusMinutes(30), AppointmentStatus.CONFIRMED));
        appointmentRepository.save(createAppointment(start.plusMinutes(30), start.plusMinutes(60), AppointmentStatus.COMPLETED));

        var result = appointmentRepository.findOfficeOccupancy(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)[0]).isEqualTo(office.getId());
        assertThat(result.get(0)[1]).isEqualTo(office.getName());
        assertThat(((Number) result.get(0)[2]).longValue()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findDoctorProductivity - calcula productividad de doctores")
    void shouldFindDoctorProductivity() {
        var from = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        var to = from.plusDays(1);
        var start = from.withHour(9).withMinute(0);

        appointmentRepository.save(createAppointment(start, start.plusMinutes(30), AppointmentStatus.COMPLETED));
        appointmentRepository.save(createAppointment(start.plusMinutes(30), start.plusMinutes(60), AppointmentStatus.COMPLETED));

        var result = appointmentRepository.findDoctorProductivity(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)[0]).isEqualTo(doctor.getId());
        assertThat(((Number) result.get(0)[4]).longValue()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findNoShowPatients - encuentra pacientes con NO_SHOW")
    void shouldFindNoShowPatients() {
        var from = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        var to = from.plusDays(1);
        var start = from.withHour(9).withMinute(0);

        appointmentRepository.save(createAppointment(start, start.plusMinutes(30), AppointmentStatus.NO_SHOW));

        var result = appointmentRepository.findNoShowPatients(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)[0]).isEqualTo(patient.getId());
        assertThat(((Number) result.get(0)[4]).longValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("countCancelledAndNoShowBySpecialty - cuenta cancelados y NO_SHOW por especialidad")
    void shouldCountCancelledAndNoShowBySpecialty() {
        var from = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        var to = from.plusDays(1);
        var start = from.withHour(9).withMinute(0);

        appointmentRepository.save(createAppointment(start, start.plusMinutes(30), AppointmentStatus.CANCELLED));
        appointmentRepository.save(createAppointment(start.plusHours(1), start.plusHours(1).plusMinutes(30), AppointmentStatus.NO_SHOW));

        var result = appointmentRepository.countCancelledAndNoShowBySpecialty(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)[0]).isEqualTo("Cardiología");
        assertThat(((Number) result.get(0)[1]).longValue()).isEqualTo(2L);
    }
}