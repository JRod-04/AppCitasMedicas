package com.citasmedicas.appcitasmedicas.RepositoryTest;


import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.DoctorScheduleRepository;
import com.citasmedicas.appcitasmedicas.Repository.SpecialtyRepository;
import com.citasmedicas.appcitasmedicas.entity.Doctor;
import com.citasmedicas.appcitasmedicas.entity.DoctorSchedule;
import com.citasmedicas.appcitasmedicas.entity.Specialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DoctorScheduleRepositoryTest {

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private Doctor doctor;
    private DoctorSchedule schedule;

    @BeforeEach
    void setUp() {
        var specialty = specialtyRepository.save(Specialty.builder()
                .name("Cardiología")
                .description("Especialidad del corazón")
                .build());

        doctor = doctorRepository.save(Doctor.builder()
                .firstName("Pedro")
                .lastName("Gil")
                .licenseNumber("LIC-100")
                .email("pedro@test.com")
                .specialty(specialty)
                .active(true)
                .build());

        schedule = doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build());
    }

    @Test
    @DisplayName("save - guarda un horario correctamente")
    void shouldSaveDoctorSchedule() {
        // Given
        var newSchedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        // When
        var saved = doctorScheduleRepository.save(newSchedule);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDoctor().getId()).isEqualTo(doctor.getId());
        assertThat(saved.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    @DisplayName("findById - encuentra un horario por ID")
    void shouldFindById() {
        // When
        var found = doctorScheduleRepository.findById(schedule.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(found.get().getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(found.get().getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    @DisplayName("findById - retorna Optional vacío cuando el ID no existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // When
        var found = doctorScheduleRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll - retorna todos los horarios")
    void shouldFindAll() {
        // Given
        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .build());

        // When
        List<DoctorSchedule> results = doctorScheduleRepository.findAll();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(DoctorSchedule::getDayOfWeek)
                .contains(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
    }

    @Test
    @DisplayName("findByDoctorId - encuentra horarios por doctor con paginación")
    void shouldFindByDoctorIdWithPagination() {
        // Given
        // Crear múltiples horarios para el mismo doctor
        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build());

        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build());

        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.THURSDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(15, 0))
                .build());

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());

        // When
        Page<DoctorSchedule> resultPage = doctorScheduleRepository.findByDoctorId(doctor.getId(), pageable);

        // Then
        assertThat(resultPage).hasSize(2);
        assertThat(resultPage.getTotalElements()).isEqualTo(4); // incluye el del setUp
        assertThat(resultPage.getContent()).allMatch(s -> s.getDoctor().getId().equals(doctor.getId()));
    }

    @Test
    @DisplayName("findByDoctorId - retorna página vacía cuando el doctor no tiene horarios")
    void shouldReturnEmptyPageWhenDoctorHasNoSchedules() {
        // Given
        var anotherSpecialty = specialtyRepository.save(Specialty.builder()
                .name("Neurología")
                .build());

        var anotherDoctor = doctorRepository.save(Doctor.builder()
                .firstName("Ana")
                .lastName("Martinez")
                .licenseNumber("LIC-200")
                .email("ana@test.com")
                .specialty(anotherSpecialty)
                .active(true)
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<DoctorSchedule> result = doctorScheduleRepository.findByDoctorId(anotherDoctor.getId(), pageable);

        // Then
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByDoctorIdAndDayOfWeek - encuentra horario por doctor y día de semana")
    void shouldFindByDoctorIdAndDayOfWeek() {
        // Given
        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .build());

        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(14, 0))
                .build());

        // When
        List<DoctorSchedule> mondaySchedules = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.MONDAY);
        List<DoctorSchedule> tuesdaySchedules = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.TUESDAY);
        List<DoctorSchedule> wednesdaySchedules = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.WEDNESDAY);
        List<DoctorSchedule> sundaySchedules = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.SUNDAY);

        // Then
        assertThat(mondaySchedules).hasSize(1);
        assertThat(mondaySchedules.get(0).getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        assertThat(tuesdaySchedules).hasSize(1);
        assertThat(tuesdaySchedules.get(0).getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);

        assertThat(wednesdaySchedules).hasSize(1);
        assertThat(wednesdaySchedules.get(0).getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);

        assertThat(sundaySchedules).isEmpty();
    }

    @Test
    @DisplayName("findByDoctorIdAndDayOfWeek - retorna lista vacía cuando no hay horario para ese día")
    void shouldReturnEmptyWhenNoScheduleForDay() {
        // When
        List<DoctorSchedule> result = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.SUNDAY);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByDoctorIdAndDayOfWeek - retorna lista vacía cuando el doctor no existe")
    void shouldReturnEmptyWhenDoctorDoesNotExist() {
        // When
        List<DoctorSchedule> result = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(999L, DayOfWeek.MONDAY);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("update - actualiza un horario existente")
    void shouldUpdateSchedule() {
        // Given
        schedule.setDayOfWeek(DayOfWeek.FRIDAY);
        schedule.setStartTime(LocalTime.of(8, 30));
        schedule.setEndTime(LocalTime.of(16, 30));

        // When
        DoctorSchedule updated = doctorScheduleRepository.save(schedule);

        // Then
        assertThat(updated.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(updated.getStartTime()).isEqualTo(LocalTime.of(8, 30));
        assertThat(updated.getEndTime()).isEqualTo(LocalTime.of(16, 30));
    }

    @Test
    @DisplayName("deleteById - elimina un horario")
    void shouldDeleteById() {
        // When
        doctorScheduleRepository.deleteById(schedule.getId());

        // Then
        var found = doctorScheduleRepository.findById(schedule.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsById - retorna true si el horario existe")
    void shouldReturnTrueWhenExists() {
        // When
        boolean exists = doctorScheduleRepository.existsById(schedule.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById - retorna false si el horario no existe")
    void shouldReturnFalseWhenNotExists() {
        // When
        boolean exists = doctorScheduleRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("count - retorna el número total de horarios")
    void shouldCountSchedules() {
        // Given
        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.THURSDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build());

        // When
        long count = doctorScheduleRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("findByDoctorIdAndDayOfWeek - múltiples horarios para el mismo doctor y día")
    void shouldReturnMultipleSchedulesForSameDoctorAndDay() {
        // Given
        // Un doctor puede tener múltiples bloques horarios el mismo día
        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .build());

        doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(18, 0))
                .build());

        // When
        List<DoctorSchedule> mondaySchedules = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.MONDAY);

        // Then
        assertThat(mondaySchedules).hasSize(3); // incluye el del setUp
        assertThat(mondaySchedules).allMatch(s -> s.getDayOfWeek() == DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("findByDoctorId - respeta la paginación correctamente")
    void shouldRespectPagination() {
        // Given
        // Crear 5 horarios para el doctor
        for (int i = 1; i <= 5; i++) {
            doctorScheduleRepository.save(DoctorSchedule.builder()
                    .doctor(doctor)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(8, i))
                    .endTime(LocalTime.of(12, i))
                    .build());
        }

        // When - página 0 con tamaño 2
        Pageable firstPageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        Page<DoctorSchedule> firstPage = doctorScheduleRepository.findByDoctorId(doctor.getId(), firstPageable);

        // When - página 1 con tamaño 2
        Pageable secondPageable = PageRequest.of(1, 2, Sort.by("id").ascending());
        Page<DoctorSchedule> secondPage = doctorScheduleRepository.findByDoctorId(doctor.getId(), secondPageable);

        // Then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isGreaterThanOrEqualTo(6);
    }
}