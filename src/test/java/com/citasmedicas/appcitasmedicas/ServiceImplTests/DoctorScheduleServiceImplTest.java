package com.citasmedicas.appcitasmedicas.ServiceImplTests;


import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import com.citasmedicas.appcitasmedicas.Entity.DoctorSchedule;
import com.citasmedicas.appcitasmedicas.Exception.BusinessException;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.DoctorScheduleRepository;
import com.citasmedicas.appcitasmedicas.Service.Impl.DoctorScheduleServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorScheduleRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorScheduleResponse;
import com.citasmedicas.appcitasmedicas.mapper.DoctorScheduleMapper;
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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorScheduleService Tests")
class DoctorScheduleServiceImplTest {

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleMapper doctorScheduleMapper;

    @InjectMocks
    private DoctorScheduleServiceImpl doctorScheduleService;

    private Doctor doctor;
    private DoctorSchedule schedule;
    private DoctorScheduleResponse scheduleResponse;
    private CreateDoctorScheduleRequest createRequest;

    @BeforeEach
    void setUp() {
        doctor = Doctor.builder()
                .id(1L)
                .firstName("Pedro")
                .lastName("Gil")
                .active(true)
                .build();

        schedule = DoctorSchedule.builder()
                .id(1L)
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        scheduleResponse = DoctorScheduleResponse.builder()
                .id(1L)
                .doctorId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        createRequest = new CreateDoctorScheduleRequest(1L,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );
    }

    @Test
    @DisplayName("create - debe crear un horario exitosamente")
    void shouldCreateScheduleSuccessfully() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenReturn(schedule);
        when(doctorScheduleMapper.toResponse(schedule)).thenReturn(scheduleResponse);

        DoctorScheduleResponse result = doctorScheduleService.create(1L, createRequest);

        assertThat(result).isNotNull();
        assertThat(result.doctorId()).isEqualTo(1L);
        verify(doctorScheduleRepository).save(any(DoctorSchedule.class));
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el doctor no existe")
    void shouldThrowWhenDoctorNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorScheduleService.create(1L, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 1");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el doctor está inactivo")
    void shouldThrowWhenDoctorIsInactive() {
        doctor.setActive(false);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorScheduleService.create(1L, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot add schedule to an inactive doctor");
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando startTime es después de endTime")
    void shouldThrowWhenStartTimeAfterEndTime() {
        var invalidRequest = new CreateDoctorScheduleRequest(1L,
                DayOfWeek.MONDAY,
                LocalTime.of(17, 0),
                LocalTime.of(9, 0)
        );
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorScheduleService.create(1L, invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Start time must be before end time");
    }

    @Test
    @DisplayName("findByDoctor - debe retornar horarios del doctor paginados")
    void shouldFindByDoctor() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DoctorSchedule> schedulePage = new PageImpl<>(List.of(schedule));

        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctorId(1L, pageable)).thenReturn(schedulePage);
        when(doctorScheduleMapper.toResponse(any(DoctorSchedule.class))).thenReturn(scheduleResponse);

        Page<DoctorScheduleResponse> result = doctorScheduleService.findByDoctor(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findByDoctor - debe lanzar excepción cuando el doctor no existe")
    void shouldThrowWhenDoctorNotFoundForFind() {
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> doctorScheduleService.findByDoctor(1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 1");
    }
}