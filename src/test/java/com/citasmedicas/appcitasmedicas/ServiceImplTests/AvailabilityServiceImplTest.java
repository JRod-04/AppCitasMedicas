package com.citasmedicas.appcitasmedicas.ServiceImplTests;


import com.citasmedicas.appcitasmedicas.Entity.Appointment;
import com.citasmedicas.appcitasmedicas.Entity.AppointmentType;
import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import com.citasmedicas.appcitasmedicas.Entity.DoctorSchedule;
import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentRepository;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentTypeRepository;
import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.DoctorScheduleRepository;
import com.citasmedicas.appcitasmedicas.Service.Impl.AvailabilityServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Response.AvailabilitySlotResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilityService Tests")
class AvailabilityServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentTypeRepository appointmentTypeRepository;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    private Doctor doctor;
    private AppointmentType appointmentType;
    private DoctorSchedule schedule;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        doctor = Doctor.builder().id(1L).active(true).build();

        appointmentType = AppointmentType.builder()
                .id(1L)
                .name("Consulta General")
                .durationMinutes(30)
                .build();

        schedule = DoctorSchedule.builder()
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        date = LocalDate.now().plusDays(1);
    }

    @Test
    @DisplayName("getAvailableSlots - debe retornar slots disponibles")
    void shouldReturnAvailableSlots() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime start = date.atTime(9, 0);
        LocalDateTime end = date.atTime(17, 0);

        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(appointmentTypeRepository.findById(1L)).thenReturn(java.util.Optional.of(appointmentType));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, date.getDayOfWeek()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findActiveDoctorAppointmentsInRange(anyLong(), any(), any(), any()))
                .thenReturn(List.of());

        Page<AvailabilitySlotResponse> result = availabilityService.getAvailableSlots(1L, date, 1L, pageable);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getAvailableSlots - debe lanzar excepción cuando el doctor no existe")
    void shouldThrowWhenDoctorNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> availabilityService.getAvailableSlots(1L, date, 1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 1");
    }

    @Test
    @DisplayName("getAvailableSlots - debe lanzar excepción cuando el tipo de cita no existe")
    void shouldThrowWhenAppointmentTypeNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(appointmentTypeRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> availabilityService.getAvailableSlots(1L, date, 1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment type not found: 1");
    }

    @Test
    @DisplayName("getAvailableSlots - debe retornar página vacía cuando no hay horarios")
    void shouldReturnEmptyPageWhenNoSchedules() {
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(appointmentTypeRepository.findById(1L)).thenReturn(java.util.Optional.of(appointmentType));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, date.getDayOfWeek()))
                .thenReturn(List.of());

        Page<AvailabilitySlotResponse> result = availabilityService.getAvailableSlots(1L, date, 1L, pageable);

        assertThat(result).isEmpty();
    }
}