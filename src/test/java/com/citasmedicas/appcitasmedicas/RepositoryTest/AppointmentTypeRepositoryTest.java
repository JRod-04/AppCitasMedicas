package com.citasmedicas.appcitasmedicas.RepositoryTest;

import com.citasmedicas.appcitasmedicas.entity.AppointmentType;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentTypeRepository;
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
class AppointmentTypeRepositoryTest {

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    private AppointmentType appointmentType;

    @BeforeEach
    void setUp() {
        appointmentType = appointmentTypeRepository.save(AppointmentType.builder()
                .name("Consulta General")
                .durationMinutes(30)
                .description("Consulta médica general")
                .build());
    }

    @Test
    @DisplayName("save - guarda un tipo de cita correctamente")
    void shouldSaveAppointmentType() {
        // Given
        var newType = AppointmentType.builder()
                .name("Cardiología")
                .durationMinutes(45)
                .description("Consulta con especialista en corazón")
                .build();

        // When
        var saved = appointmentTypeRepository.save(newType);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Cardiología");
        assertThat(saved.getDurationMinutes()).isEqualTo(45);
        assertThat(saved.getDescription()).isEqualTo("Consulta con especialista en corazón");
    }

    @Test
    @DisplayName("findById - encuentra un tipo de cita por ID")
    void shouldFindById() {
        // When
        Optional<AppointmentType> found = appointmentTypeRepository.findById(appointmentType.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Consulta General");
        assertThat(found.get().getDurationMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("findById - retorna Optional vacío cuando el ID no existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // When
        Optional<AppointmentType> found = appointmentTypeRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll - retorna todos los tipos de cita")
    void shouldFindAll() {
        // Given
        appointmentTypeRepository.save(AppointmentType.builder()
                .name("Urgencia")
                .durationMinutes(20)
                .description("Atención inmediata")
                .build());

        appointmentTypeRepository.save(AppointmentType.builder()
                .name("Control")
                .durationMinutes(15)
                .description("Control de seguimiento")
                .build());

        // When
        List<AppointmentType> results = appointmentTypeRepository.findAll();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(AppointmentType::getName)
                .contains("Consulta General", "Urgencia", "Control");
    }

    @Test
    @DisplayName("deleteById - elimina un tipo de cita")
    void shouldDeleteById() {
        // When
        appointmentTypeRepository.deleteById(appointmentType.getId());

        // Then
        Optional<AppointmentType> found = appointmentTypeRepository.findById(appointmentType.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsById - retorna true si el tipo de cita existe")
    void shouldReturnTrueWhenExistsById() {
        // When
        boolean exists = appointmentTypeRepository.existsById(appointmentType.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById - retorna false si el tipo de cita no existe")
    void shouldReturnFalseWhenNotExistsById() {
        // When
        boolean exists = appointmentTypeRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("count - retorna el número total de tipos de cita")
    void shouldCountAppointmentTypes() {
        // Given
        appointmentTypeRepository.save(AppointmentType.builder()
                .name("Odontología")
                .durationMinutes(40)
                .build());

        // When
        long count = appointmentTypeRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("save - actualiza un tipo de cita existente")
    void shouldUpdateExistingAppointmentType() {
        // Given
        appointmentType.setName("Consulta Especializada");
        appointmentType.setDurationMinutes(60);

        // When
        AppointmentType updated = appointmentTypeRepository.save(appointmentType);

        // Then
        assertThat(updated.getId()).isEqualTo(appointmentType.getId());
        assertThat(updated.getName()).isEqualTo("Consulta Especializada");
        assertThat(updated.getDurationMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("deleteAll - elimina todos los tipos de cita")
    void shouldDeleteAll() {
        // When
        appointmentTypeRepository.deleteAll();

        // Then
        List<AppointmentType> results = appointmentTypeRepository.findAll();
        assertThat(results).isEmpty();
    }
}