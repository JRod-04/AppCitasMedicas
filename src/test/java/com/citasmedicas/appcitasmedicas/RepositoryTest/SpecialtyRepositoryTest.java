package com.citasmedicas.appcitasmedicas.RepositoryTest;


import com.citasmedicas.appcitasmedicas.Repository.SpecialtyRepository;
import com.citasmedicas.appcitasmedicas.Entity.Specialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SpecialtyRepositoryTest {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private Specialty specialty;

    @BeforeEach
    void setUp() {
        specialty = specialtyRepository.save(Specialty.builder()
                .name("Cardiología")
                .description("Especialidad médica del corazón y sistema circulatorio")
                .build());
    }

    @Test
    @DisplayName("save - guarda una especialidad correctamente")
    void shouldSaveSpecialty() {
        // Given
        var newSpecialty = Specialty.builder()
                .name("Neurología")
                .description("Estudio del sistema nervioso")
                .build();

        // When
        var saved = specialtyRepository.save(newSpecialty);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Neurología");
        assertThat(saved.getDescription()).isEqualTo("Estudio del sistema nervioso");
    }

    @Test
    @DisplayName("findById - encuentra una especialidad por ID")
    void shouldFindById() {
        // When
        var found = specialtyRepository.findById(specialty.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Cardiología");
        assertThat(found.get().getDescription()).isEqualTo("Especialidad médica del corazón y sistema circulatorio");
    }

    @Test
    @DisplayName("findById - retorna Optional vacío cuando el ID no existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // When
        var found = specialtyRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll - retorna todas las especialidades")
    void shouldFindAll() {
        // Given
        specialtyRepository.save(Specialty.builder()
                .name("Dermatología")
                .description("Estudio de la piel")
                .build());

        specialtyRepository.save(Specialty.builder()
                .name("Traumatología")
                .description("Estudio del sistema musculoesquelético")
                .build());

        // When
        var results = specialtyRepository.findAll();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Specialty::getName)
                .contains("Cardiología", "Dermatología", "Traumatología");
    }

    @Test
    @DisplayName("findByNameIgnoreCase - encuentra especialidad por nombre ignorando mayúsculas/minúsculas")
    void shouldFindByNameIgnoreCase() {
        // When
        Optional<Specialty> foundLower = specialtyRepository.findByNameIgnoreCase("cardiología");
        Optional<Specialty> foundUpper = specialtyRepository.findByNameIgnoreCase("CARDIOLOGÍA");
        Optional<Specialty> foundMixed = specialtyRepository.findByNameIgnoreCase("CarDiOlOgÍa");

        // Then
        assertThat(foundLower).isPresent();
        assertThat(foundLower.get().getName()).isEqualTo("Cardiología");

        assertThat(foundUpper).isPresent();
        assertThat(foundUpper.get().getName()).isEqualTo("Cardiología");

        assertThat(foundMixed).isPresent();
        assertThat(foundMixed.get().getName()).isEqualTo("Cardiología");
    }

    @Test
    @DisplayName("findByNameIgnoreCase - retorna Optional vacío cuando el nombre no existe")
    void shouldReturnEmptyWhenNameNotFound() {
        // When
        Optional<Specialty> found = specialtyRepository.findByNameIgnoreCase("EspecialidadInexistente");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("update - actualiza una especialidad existente")
    void shouldUpdateSpecialty() {
        // Given
        specialty.setName("Cardiología Avanzada");
        specialty.setDescription("Especialidad avanzada del corazón");

        // When
        Specialty updated = specialtyRepository.save(specialty);

        // Then
        assertThat(updated.getName()).isEqualTo("Cardiología Avanzada");
        assertThat(updated.getDescription()).isEqualTo("Especialidad avanzada del corazón");
    }

    @Test
    @DisplayName("deleteById - elimina una especialidad")
    void shouldDeleteById() {
        // When
        specialtyRepository.deleteById(specialty.getId());

        // Then
        var found = specialtyRepository.findById(specialty.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsById - retorna true si la especialidad existe")
    void shouldReturnTrueWhenExists() {
        // When
        boolean exists = specialtyRepository.existsById(specialty.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById - retorna false si la especialidad no existe")
    void shouldReturnFalseWhenNotExists() {
        // When
        boolean exists = specialtyRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("count - retorna el número total de especialidades")
    void shouldCountSpecialties() {
        // Given
        specialtyRepository.save(Specialty.builder()
                .name("Oftalmología")
                .description("Estudio de los ojos")
                .build());

        // When
        long count = specialtyRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("save - valida que el nombre sea único")
    void shouldEnforceUniqueNameConstraint() {
        // Given
        var duplicateSpecialty = Specialty.builder()
                .name("Cardiología")
                .description("Descripción duplicada")
                .build();

        // When / Then
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            specialtyRepository.save(duplicateSpecialty);
            specialtyRepository.flush();
        });
    }
}