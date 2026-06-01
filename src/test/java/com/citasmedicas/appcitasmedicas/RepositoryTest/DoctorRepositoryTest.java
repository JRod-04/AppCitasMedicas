package com.citasmedicas.appcitasmedicas.RepositoryTest;


import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.SpecialtyRepository;
import com.citasmedicas.appcitasmedicas.entity.Doctor;
import com.citasmedicas.appcitasmedicas.entity.Specialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private Specialty specialty;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        specialty = specialtyRepository.save(Specialty.builder()
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
    }

    @Test
    @DisplayName("save - guarda un doctor correctamente")
    void shouldSaveDoctor() {
        // Given
        var newDoctor = Doctor.builder()
                .firstName("Maria")
                .lastName("Lopez")
                .licenseNumber("LIC-200")
                .email("maria@test.com")
                .specialty(specialty)
                .active(true)
                .build();

        // When
        var saved = doctorRepository.save(newDoctor);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Maria");
        assertThat(saved.getLastName()).isEqualTo("Lopez");
        assertThat(saved.getLicenseNumber()).isEqualTo("LIC-200");
        assertThat(saved.getEmail()).isEqualTo("maria@test.com");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getSpecialty().getId()).isEqualTo(specialty.getId());
    }

    @Test
    @DisplayName("findById - encuentra un doctor por ID")
    void shouldFindById() {
        // When
        var found = doctorRepository.findById(doctor.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Pedro");
        assertThat(found.get().getLastName()).isEqualTo("Gil");
        assertThat(found.get().getLicenseNumber()).isEqualTo("LIC-100");
    }

    @Test
    @DisplayName("findById - retorna Optional vacío cuando el ID no existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // When
        var found = doctorRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll - retorna todos los doctores")
    void shouldFindAll() {
        // Given
        var anotherSpecialty = specialtyRepository.save(Specialty.builder()
                .name("Neurología")
                .build());

        doctorRepository.save(Doctor.builder()
                .firstName("Carlos")
                .lastName("Ruiz")
                .licenseNumber("LIC-300")
                .email("carlos@test.com")
                .specialty(anotherSpecialty)
                .active(true)
                .build());

        // When
        List<Doctor> results = doctorRepository.findAll();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Doctor::getFirstName)
                .contains("Pedro", "Carlos");
    }

    @Test
    @DisplayName("findByActiveTrue - encuentra solo doctores activos")
    void shouldFindActiveDoctors() {
        // Given
        var inactiveDoctor = doctorRepository.save(Doctor.builder()
                .firstName("Inactivo")
                .lastName("Test")
                .licenseNumber("LIC-999")
                .email("inactive@test.com")
                .specialty(specialty)
                .active(false)
                .build());

        // When
        List<Doctor> activeDoctors = doctorRepository.findByActiveTrue();

        // Then
        assertThat(activeDoctors).hasSize(1);
        assertThat(activeDoctors.get(0).getId()).isEqualTo(doctor.getId());
        assertThat(activeDoctors.get(0).isActive()).isTrue();
        assertThat(activeDoctors).doesNotContain(inactiveDoctor);
    }

    @Test
    @DisplayName("findBySpecialtyIdAndActiveTrue - encuentra doctores activos por especialidad con paginación")
    void shouldFindActiveDoctorsBySpecialtyWithPagination() {
        // Given
        var anotherSpecialty = specialtyRepository.save(Specialty.builder()
                .name("Dermatología")
                .build());

        // Doctores activos en Cardiología
        doctorRepository.save(Doctor.builder()
                .firstName("Ana")
                .lastName("Martinez")
                .licenseNumber("LIC-400")
                .email("ana@test.com")
                .specialty(specialty)
                .active(true)
                .build());

        doctorRepository.save(Doctor.builder()
                .firstName("Luis")
                .lastName("Gomez")
                .licenseNumber("LIC-500")
                .email("luis@test.com")
                .specialty(specialty)
                .active(true)
                .build());

        // Doctor activo en otra especialidad (no debe aparecer)
        doctorRepository.save(Doctor.builder()
                .firstName("Sofia")
                .lastName("Diaz")
                .licenseNumber("LIC-600")
                .email("sofia@test.com")
                .specialty(anotherSpecialty)
                .active(true)
                .build());

        // Doctor inactivo en Cardiología (no debe aparecer)
        doctorRepository.save(Doctor.builder()
                .firstName("Inactivo")
                .lastName("Cardio")
                .licenseNumber("LIC-700")
                .email("inactivo@test.com")
                .specialty(specialty)
                .active(false)
                .build());

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // When
        List<Doctor> result = doctorRepository.findBySpecialtyIdAndActiveTrue(specialty.getId(), pageable);

        // Then
        assertThat(result).hasSize(3); // doctor inicial + Ana + Luis
        assertThat(result).allMatch(d -> d.getSpecialty().getId().equals(specialty.getId()));
        assertThat(result).allMatch(Doctor::isActive);
        assertThat(result).extracting(Doctor::getFirstName)
                .contains("Pedro", "Ana", "Luis")
                .doesNotContain("Sofia", "InactivoCardio");
    }

    @Test
    @DisplayName("findBySpecialtyIdAndActiveTrue - retorna lista vacía si no hay doctores activos en esa especialidad")
    void shouldReturnEmptyWhenNoActiveDoctorsInSpecialty() {
        // Given
        var emptySpecialty = specialtyRepository.save(Specialty.builder()
                .name("Especialidad Vacía")
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // When
        List<Doctor> result = doctorRepository.findBySpecialtyIdAndActiveTrue(emptySpecialty.getId(), pageable);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findBySpecialtyIdAndActiveTrue - respeta la paginación")
    void shouldRespectPagination() {
        // Given
        // Crear 5 doctores activos en Cardiología
        for (int i = 1; i <= 5; i++) {
            doctorRepository.save(Doctor.builder()
                    .firstName("Doctor" + i)
                    .lastName("Apellido" + i)
                    .licenseNumber("LIC-PAGE-" + i)
                    .email("doctor" + i + "@test.com")
                    .specialty(specialty)
                    .active(true)
                    .build());
        }

        // When - página 0 con tamaño 2
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        List<Doctor> firstPage = doctorRepository.findBySpecialtyIdAndActiveTrue(specialty.getId(), pageable);

        // When - página 1 con tamaño 2
        pageable = PageRequest.of(1, 2, Sort.by("id").ascending());
        List<Doctor> secondPage = doctorRepository.findBySpecialtyIdAndActiveTrue(specialty.getId(), pageable);

        // Then
        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);

        // Verificar que son diferentes
        assertThat(firstPage.get(0).getId()).isNotEqualTo(secondPage.get(0).getId());
    }

    @Test
    @DisplayName("update - actualiza un doctor existente")
    void shouldUpdateDoctor() {
        // Given
        var newSpecialty = specialtyRepository.save(Specialty.builder()
                .name("Cirugía")
                .build());

        // When
        doctor.setFirstName("Pedro Actualizado");
        doctor.setLastName("Gill Actualizado");
        doctor.setEmail("pedro.actualizado@test.com");
        doctor.setSpecialty(newSpecialty);
        doctor.setActive(false);

        Doctor updated = doctorRepository.save(doctor);

        // Then
        assertThat(updated.getFirstName()).isEqualTo("Pedro Actualizado");
        assertThat(updated.getLastName()).isEqualTo("Gill Actualizado");
        assertThat(updated.getEmail()).isEqualTo("pedro.actualizado@test.com");
        assertThat(updated.getSpecialty().getName()).isEqualTo("Cirugía");
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @DisplayName("deleteById - elimina un doctor")
    void shouldDeleteById() {
        // When
        doctorRepository.deleteById(doctor.getId());

        // Then
        var found = doctorRepository.findById(doctor.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsById - retorna true si el doctor existe")
    void shouldReturnTrueWhenExists() {
        // When
        boolean exists = doctorRepository.existsById(doctor.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById - retorna false si el doctor no existe")
    void shouldReturnFalseWhenNotExists() {
        // When
        boolean exists = doctorRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("count - retorna el número total de doctores")
    void shouldCountDoctors() {
        // Given
        doctorRepository.save(Doctor.builder()
                .firstName("Nuevo")
                .lastName("Doctor")
                .licenseNumber("LIC-NEW")
                .email("new@test.com")
                .specialty(specialty)
                .active(true)
                .build());

        // When
        long count = doctorRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}
