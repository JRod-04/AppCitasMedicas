package com.citasmedicas.appcitasmedicas.RepositoryTest;


import com.citasmedicas.appcitasmedicas.Repository.OfficeRepository;
import com.citasmedicas.appcitasmedicas.entity.Office;
import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OfficeRepositoryTest {

    @Autowired
    private OfficeRepository officeRepository;

    private Office office;

    @BeforeEach
    void setUp() {
        office = officeRepository.save(Office.builder()
                .name("Consultorio 101")
                .location("Piso 1")
                .floor("101")
                .status(OfficeStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("save - guarda un consultorio correctamente")
    void shouldSaveOffice() {
        // Given
        var newOffice = Office.builder()
                .name("Consultorio 202")
                .location("Piso 2")
                .floor("202")
                .status(OfficeStatus.ACTIVE)
                .build();

        // When
        var saved = officeRepository.save(newOffice);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Consultorio 202");
        assertThat(saved.getLocation()).isEqualTo("Piso 2");
        assertThat(saved.getFloor()).isEqualTo("202");
        assertThat(saved.getStatus()).isEqualTo(OfficeStatus.ACTIVE);
    }

    @Test
    @DisplayName("findById - encuentra un consultorio por ID")
    void shouldFindById() {
        // When
        var found = officeRepository.findById(office.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Consultorio 101");
        assertThat(found.get().getStatus()).isEqualTo(OfficeStatus.ACTIVE);
    }

    @Test
    @DisplayName("findById - retorna Optional vacío cuando el ID no existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // When
        var found = officeRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll - retorna todos los consultorios")
    void shouldFindAll() {
        // Given
        officeRepository.save(Office.builder()
                .name("Consultorio 102")
                .location("Piso 1")
                .floor("102")
                .status(OfficeStatus.ACTIVE)
                .build());

        officeRepository.save(Office.builder()
                .name("Consultorio 103")
                .location("Piso 1")
                .floor("103")
                .status(OfficeStatus.INACTIVE)
                .build());

        // When
        List<Office> results = officeRepository.findAll();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Office::getName)
                .contains("Consultorio 101", "Consultorio 102", "Consultorio 103");
    }

    @Test
    @DisplayName("findByStatus - encuentra consultorios por estado ACTIVO")
    void shouldFindActiveOfficesByStatus() {
        // Given
        officeRepository.save(Office.builder()
                .name("Consultorio 102")
                .location("Piso 1")
                .floor("102")
                .status(OfficeStatus.ACTIVE)
                .build());

        officeRepository.save(Office.builder()
                .name("Consultorio 103")
                .location("Piso 1")
                .floor("103")
                .status(OfficeStatus.INACTIVE)
                .build());

        officeRepository.save(Office.builder()
                .name("Consultorio 104")
                .location("Piso 2")
                .floor("204")
                .status(OfficeStatus.MAINTENANCE)
                .build());

        // When
        List<Office> activeOffices = officeRepository.findByStatus(OfficeStatus.ACTIVE);
        List<Office> inactiveOffices = officeRepository.findByStatus(OfficeStatus.INACTIVE);
        List<Office> maintenanceOffices = officeRepository.findByStatus(OfficeStatus.MAINTENANCE);

        // Then
        assertThat(activeOffices).hasSize(2);
        assertThat(activeOffices).allMatch(o -> o.getStatus() == OfficeStatus.ACTIVE);
        assertThat(activeOffices).extracting(Office::getName)
                .contains("Consultorio 101", "Consultorio 102");

        assertThat(inactiveOffices).hasSize(1);
        assertThat(inactiveOffices.get(0).getStatus()).isEqualTo(OfficeStatus.INACTIVE);

        assertThat(maintenanceOffices).hasSize(1);
        assertThat(maintenanceOffices.get(0).getStatus()).isEqualTo(OfficeStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("findByStatus - retorna lista vacía cuando no hay consultorios con ese estado")
    void shouldReturnEmptyWhenNoOfficesWithStatus() {
        // When
        List<Office> result = officeRepository.findByStatus(OfficeStatus.MAINTENANCE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("update - actualiza un consultorio existente")
    void shouldUpdateOffice() {
        // Given
        office.setName("Consultorio 101 Actualizado");
        office.setLocation("Piso 1 Actualizado");
        office.setFloor("101A");
        office.setStatus(OfficeStatus.MAINTENANCE);

        // When
        Office updated = officeRepository.save(office);

        // Then
        assertThat(updated.getName()).isEqualTo("Consultorio 101 Actualizado");
        assertThat(updated.getLocation()).isEqualTo("Piso 1 Actualizado");
        assertThat(updated.getFloor()).isEqualTo("101A");
        assertThat(updated.getStatus()).isEqualTo(OfficeStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("deleteById - elimina un consultorio")
    void shouldDeleteById() {
        // When
        officeRepository.deleteById(office.getId());

        // Then
        var found = officeRepository.findById(office.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsById - retorna true si el consultorio existe")
    void shouldReturnTrueWhenExists() {
        // When
        boolean exists = officeRepository.existsById(office.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById - retorna false si el consultorio no existe")
    void shouldReturnFalseWhenNotExists() {
        // When
        boolean exists = officeRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("count - retorna el número total de consultorios")
    void shouldCountOffices() {
        // Given
        officeRepository.save(Office.builder()
                .name("Consultorio 102")
                .location("Piso 1")
                .floor("102")
                .status(OfficeStatus.ACTIVE)
                .build());

        // When
        long count = officeRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}