package com.citasmedicas.appcitasmedicas.ServiceImplTests;


import com.citasmedicas.appcitasmedicas.Entity.Office;
import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.OfficeRepository;
import com.citasmedicas.appcitasmedicas.Service.Impl.OfficeServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeResponse;
import com.citasmedicas.appcitasmedicas.mapper.OfficeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OfficeService Tests")
class OfficeServiceImplTest {

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private OfficeMapper officeMapper;

    @InjectMocks
    private OfficeServiceImpl officeService;

    private Office office;
    private OfficeResponse officeResponse;
    private CreateOfficeRequest createRequest;
    private UpdateOfficeRequest updateRequest;

    @BeforeEach
    void setUp() {
        office = Office.builder()
                .id(1L)
                .name("Consultorio 101")
                .location("Piso 1")
                .floor("101")
                .status(OfficeStatus.ACTIVE)
                .build();

        officeResponse = OfficeResponse.builder()
                .id(1L)
                .name("Consultorio 101")
                .location("Piso 1")
                .floor("101")
                .status(OfficeStatus.ACTIVE)
                .build();

        createRequest = new CreateOfficeRequest(
                "Consultorio 101", "Piso 1", "101", OfficeStatus.ACTIVE
        );

        updateRequest = new UpdateOfficeRequest(
                JsonNullable.of("Consultorio 101 Actualizado"),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        );
    }

    @Test
    @DisplayName("create - debe crear un consultorio exitosamente")
    void shouldCreateOfficeSuccessfully() {
        when(officeRepository.save(any(Office.class))).thenReturn(office);
        when(officeMapper.toResponse(office)).thenReturn(officeResponse);

        OfficeResponse result = officeService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Consultorio 101");
        verify(officeRepository).save(any(Office.class));
    }

    @Test
    @DisplayName("findAll - debe retornar página de consultorios")
    void shouldFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Office> officePage = new PageImpl<>(List.of(office));

        when(officeRepository.findAll(pageable)).thenReturn(officePage);
        when(officeMapper.toResponse(any(Office.class))).thenReturn(officeResponse);

        Page<OfficeResponse> result = officeService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("update - debe actualizar un consultorio exitosamente")
    void shouldUpdateOffice() {
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(officeRepository.save(any(Office.class))).thenReturn(office);
        when(officeMapper.toResponse(office)).thenReturn(officeResponse);

        OfficeResponse result = officeService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(office.getName()).isEqualTo("Consultorio 101 Actualizado");
    }

    @Test
    @DisplayName("update - debe lanzar excepción cuando el consultorio no existe")
    void shouldThrowWhenOfficeNotFound() {
        when(officeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> officeService.update(1L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Office not found with id: 1");
    }
}