package com.citasmedicas.appcitasmedicas.RepositoryTest;


import com.citasmedicas.appcitasmedicas.Repository.PatientRepository;
import com.citasmedicas.appcitasmedicas.Entity.Patient;
import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = patientRepository.save(Patient.builder()
                .firstName("Ana")
                .lastName("Torres")
                .documentNumber("11111111")
                .email("ana@test.com")
                .phone("3001234567")
                .status(PatientStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("save - guarda un paciente correctamente")
    void shouldSavePatient() {
        // Given
        var newPatient = Patient.builder()
                .firstName("Carlos")
                .lastName("Ruiz")
                .documentNumber("22222222")
                .email("carlos@test.com")
                .phone("3010000000")
                .status(PatientStatus.ACTIVE)
                .build();

        // When
        var saved = patientRepository.save(newPatient);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Carlos");
        assertThat(saved.getLastName()).isEqualTo("Ruiz");
        assertThat(saved.getDocumentNumber()).isEqualTo("22222222");
        assertThat(saved.getEmail()).isEqualTo("carlos@test.com");
        assertThat(saved.getPhone()).isEqualTo("3010000000");
        assertThat(saved.getStatus()).isEqualTo(PatientStatus.ACTIVE);
    }

    @Test
    @DisplayName("findById - encuentra un paciente por ID")
    void shouldFindById() {
        // When
        var found = patientRepository.findById(patient.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Ana");
        assertThat(found.get().getDocumentNumber()).isEqualTo("11111111");
    }

    @Test
    @DisplayName("findById - retorna Optional vacío cuando el ID no existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // When
        var found = patientRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll - retorna todos los pacientes")
    void shouldFindAll() {
        // Given
        patientRepository.save(Patient.builder()
                .firstName("Luis")
                .lastName("Gomez")
                .documentNumber("33333333")
                .email("luis@test.com")
                .phone("3020000000")
                .status(PatientStatus.ACTIVE)
                .build());

        patientRepository.save(Patient.builder()
                .firstName("Maria")
                .lastName("Diaz")
                .documentNumber("44444444")
                .email("maria@test.com")
                .phone("3030000000")
                .status(PatientStatus.INACTIVE)
                .build());

        // When
        List<Patient> results = patientRepository.findAll();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Patient::getFirstName)
                .contains("Ana", "Luis", "Maria");
    }

    @Test
    @DisplayName("findByDocumentNumber - encuentra paciente por número de documento")
    void shouldFindByDocumentNumber() {
        // When
        Optional<Patient> found = patientRepository.findByDocumentNumber("11111111");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Ana");
        assertThat(found.get().getDocumentNumber()).isEqualTo("11111111");
    }

    @Test
    @DisplayName("findByDocumentNumber - retorna Optional vacío cuando el documento no existe")
    void shouldReturnEmptyWhenDocumentNumberNotFound() {
        // When
        Optional<Patient> found = patientRepository.findByDocumentNumber("99999999");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByDocumentNumber - es case sensitive exacto")
    void shouldFindByDocumentNumberExactMatch() {
        // Given
        patientRepository.save(Patient.builder()
                .firstName("Test")
                .lastName("Case")
                .documentNumber("ABC123")
                .email("test@test.com")
                .phone("999999999")
                .status(PatientStatus.ACTIVE)
                .build());

        // When
        Optional<Patient> foundExact = patientRepository.findByDocumentNumber("ABC123");
        Optional<Patient> foundDifferent = patientRepository.findByDocumentNumber("abc123");

        // Then
        assertThat(foundExact).isPresent();
        assertThat(foundDifferent).isEmpty();
    }

    @Test
    @DisplayName("findByStatus - encuentra pacientes por estado ACTIVO")
    void shouldFindActivePatientsByStatus() {
        // Given
        patientRepository.save(Patient.builder()
                .firstName("Luis")
                .lastName("Gomez")
                .documentNumber("33333333")
                .email("luis@test.com")
                .phone("3020000000")
                .status(PatientStatus.ACTIVE)
                .build());

        patientRepository.save(Patient.builder()
                .firstName("Maria")
                .lastName("Diaz")
                .documentNumber("44444444")
                .email("maria@test.com")
                .phone("3030000000")
                .status(PatientStatus.INACTIVE)
                .build());

        patientRepository.save(Patient.builder()
                .firstName("Juan")
                .lastName("Perez")
                .documentNumber("55555555")
                .email("juan@test.com")
                .phone("3040000000")
                .status(PatientStatus.BLOCKED)
                .build());

        // When
        List<Patient> activePatients = patientRepository.findByStatus(PatientStatus.ACTIVE);
        List<Patient> inactivePatients = patientRepository.findByStatus(PatientStatus.INACTIVE);
        List<Patient> blockedPatients = patientRepository.findByStatus(PatientStatus.BLOCKED);

        // Then
        assertThat(activePatients).hasSize(2);
        assertThat(activePatients).allMatch(p -> p.getStatus() == PatientStatus.ACTIVE);
        assertThat(activePatients).extracting(Patient::getFirstName)
                .contains("Ana", "Luis");

        assertThat(inactivePatients).hasSize(1);
        assertThat(inactivePatients.get(0).getStatus()).isEqualTo(PatientStatus.INACTIVE);

        assertThat(blockedPatients).hasSize(1);
        assertThat(blockedPatients.get(0).getStatus()).isEqualTo(PatientStatus.BLOCKED);
    }

    @Test
    @DisplayName("findByStatus - retorna lista vacía cuando no hay pacientes con ese estado")
    void shouldReturnEmptyWhenNoPatientsWithStatus() {
        // When
        List<Patient> result = patientRepository.findByStatus(PatientStatus.BLOCKED);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("update - actualiza un paciente existente")
    void shouldUpdatePatient() {
        // Given
        patient.setFirstName("Ana Actualizada");
        patient.setLastName("Torres Actualizada");
        patient.setEmail("ana.actualizada@test.com");
        patient.setPhone("999888777");
        patient.setStatus(PatientStatus.INACTIVE);

        // When
        Patient updated = patientRepository.save(patient);

        // Then
        assertThat(updated.getFirstName()).isEqualTo("Ana Actualizada");
        assertThat(updated.getLastName()).isEqualTo("Torres Actualizada");
        assertThat(updated.getEmail()).isEqualTo("ana.actualizada@test.com");
        assertThat(updated.getPhone()).isEqualTo("999888777");
        assertThat(updated.getStatus()).isEqualTo(PatientStatus.INACTIVE);
    }

    @Test
    @DisplayName("deleteById - elimina un paciente")
    void shouldDeleteById() {
        // When
        patientRepository.deleteById(patient.getId());

        // Then
        var found = patientRepository.findById(patient.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsById - retorna true si el paciente existe")
    void shouldReturnTrueWhenExists() {
        // When
        boolean exists = patientRepository.existsById(patient.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById - retorna false si el paciente no existe")
    void shouldReturnFalseWhenNotExists() {
        // When
        boolean exists = patientRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("count - retorna el número total de pacientes")
    void shouldCountPatients() {
        // Given
        patientRepository.save(Patient.builder()
                .firstName("Nuevo")
                .lastName("Paciente")
                .documentNumber("66666666")
                .email("nuevo@test.com")
                .phone("555555555")
                .status(PatientStatus.ACTIVE)
                .build());

        // When
        long count = patientRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}