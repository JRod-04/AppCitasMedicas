package com.citasmedicas.appcitasmedicas.ServiceImplTests;


import com.citasmedicas.appcitasmedicas.Entity.AppointmentType;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentTypeRepository;
import com.citasmedicas.appcitasmedicas.Service.Impl.AppointmentTypeServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentTypeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentTypeResponse;
import com.citasmedicas.appcitasmedicas.mapper.AppointmentTypeMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentTypeService Tests")
class AppointmentTypeServiceImplTest {

    @Mock
    private AppointmentTypeRepository appointmentTypeRepository;

    @Mock
    private AppointmentTypeMapper appointmentTypeMapper;

    @InjectMocks
    private AppointmentTypeServiceImpl appointmentTypeService;

    private AppointmentType appointmentType;
    private AppointmentTypeResponse appointmentTypeResponse;
    private CreateAppointmentTypeRequest createRequest;

    @BeforeEach
    void setUp() {
        appointmentType = AppointmentType.builder()
                .id(1L)
                .name("Consulta General")
                .durationMinutes(30)
                .description("Consulta médica general")
                .build();

        appointmentTypeResponse = AppointmentTypeResponse.builder()
                .id(1L)
                .name("Consulta General")
                .durationMinutes(30)
                .description("Consulta médica general")
                .build();

        createRequest = new CreateAppointmentTypeRequest(
                "Consulta General",
                30,
                "Consulta médica general"
        );
    }

    @Test
    @DisplayName("create - debe crear un tipo de cita exitosamente")
    void shouldCreateAppointmentTypeSuccessfully() {
        when(appointmentTypeRepository.save(any(AppointmentType.class))).thenReturn(appointmentType);
        when(appointmentTypeMapper.toResponse(appointmentType)).thenReturn(appointmentTypeResponse);

        AppointmentTypeResponse result = appointmentTypeService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Consulta General");
        assertThat(result.durationMinutes()).isEqualTo(30);
        verify(appointmentTypeRepository).save(any(AppointmentType.class));
    }

    @Test
    @DisplayName("findAll - debe retornar página de tipos de cita")
    void shouldFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppointmentType> appointmentTypePage = new PageImpl<>(List.of(appointmentType));

        when(appointmentTypeRepository.findAll(pageable)).thenReturn(appointmentTypePage);
        when(appointmentTypeMapper.toResponse(any(AppointmentType.class))).thenReturn(appointmentTypeResponse);

        Page<AppointmentTypeResponse> result = appointmentTypeService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }
}