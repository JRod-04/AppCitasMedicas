package com.citasmedicas.appcitasmedicas.ServiceImplTests;

import com.citasmedicas.appcitasmedicas.Entity.Specialty;
import com.citasmedicas.appcitasmedicas.Exception.ConflictException;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.SpecialtyRepository;
import com.citasmedicas.appcitasmedicas.Service.Impl.SpecialtyServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateSpecialtyRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.SpecialtyResponse;
import com.citasmedicas.appcitasmedicas.mapper.SpecialtyMapper;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialtyService Tests")
class SpecialtyServiceImplTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private SpecialtyMapper specialtyMapper;

    @InjectMocks
    private SpecialtyServiceImpl specialtyService;

    private Specialty specialty;
    private SpecialtyResponse specialtyResponse;
    private CreateSpecialtyRequest createRequest;

    @BeforeEach
    void setUp() {
        specialty = Specialty.builder()
                .id(1L)
                .name("Cardiología")
                .description("Especialidad del corazón")
                .build();

        specialtyResponse = SpecialtyResponse.builder()
                .id(1L)
                .name("Cardiología")
                .description("Especialidad del corazón")
                .build();

        createRequest = new CreateSpecialtyRequest(
                "Cardiología",
                "Especialidad del corazón"
        );
    }

    @Test
    @DisplayName("create - debe crear una especialidad exitosamente")
    void shouldCreateSpecialtySuccessfully() {
        when(specialtyRepository.findByNameIgnoreCase("Cardiología")).thenReturn(Optional.empty());
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(specialty);
        when(specialtyMapper.toResponse(specialty)).thenReturn(specialtyResponse);

        SpecialtyResponse result = specialtyService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Cardiología");
        verify(specialtyRepository).save(any(Specialty.class));
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando la especialidad ya existe")
    void shouldThrowWhenSpecialtyAlreadyExists() {
        when(specialtyRepository.findByNameIgnoreCase("Cardiología")).thenReturn(Optional.of(specialty));

        assertThatThrownBy(() -> specialtyService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Specialty already exists: Cardiología");
    }

    @Test
    @DisplayName("findAll - debe retornar página de especialidades")
    void shouldFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Specialty> specialtyPage = new PageImpl<>(List.of(specialty));

        when(specialtyRepository.findAll(pageable)).thenReturn(specialtyPage);
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(specialtyResponse);

        Page<SpecialtyResponse> result = specialtyService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById - debe encontrar una especialidad por ID")
    void shouldFindById() {
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));
        when(specialtyMapper.toResponse(specialty)).thenReturn(specialtyResponse);

        SpecialtyResponse result = specialtyService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - debe lanzar excepción cuando la especialidad no existe")
    void shouldThrowWhenSpecialtyNotFound() {
        when(specialtyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> specialtyService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found with id: 1");
    }
}