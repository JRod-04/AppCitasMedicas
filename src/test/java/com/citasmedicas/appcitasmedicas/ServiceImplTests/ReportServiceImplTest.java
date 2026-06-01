package com.citasmedicas.appcitasmedicas.ServiceImplTests;

import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentRepository;
import com.citasmedicas.appcitasmedicas.Service.Impl.ReportServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorProductivityResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.NoShowPatientResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeOccupancyResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Tests")
class ReportServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private LocalDate from;
    private LocalDate to;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        from = LocalDate.of(2024, 1, 1);
        to = LocalDate.of(2024, 1, 31);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("getOfficeOccupancy - debe retornar ocupación de consultorios")
    void shouldGetOfficeOccupancy() {
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{1L, "Consultorio 101", 10L});

        when(appointmentRepository.findOfficeOccupancy(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(AppointmentStatus.CANCELLED)))
                .thenReturn(mockResult);

        Page<OfficeOccupancyResponse> result = reportService.getOfficeOccupancy(from, to, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).officeId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).officeName()).isEqualTo("Consultorio 101");
        assertThat(result.getContent().get(0).appointmentCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getDoctorProductivity - debe retornar productividad de doctores")
    void shouldGetDoctorProductivity() {
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{1L, "Pedro", "Gil", "Cardiología", 5L});

        when(appointmentRepository.findDoctorProductivity(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(AppointmentStatus.COMPLETED)))
                .thenReturn(mockResult);

        Page<DoctorProductivityResponse> result = reportService.getDoctorProductivity(from, to, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).doctorId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).doctorName()).isEqualTo("Pedro Gil");
        assertThat(result.getContent().get(0).specialtyName()).isEqualTo("Cardiología");
        assertThat(result.getContent().get(0).completedAppointments()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getNoShowPatients - debe retornar pacientes con NO_SHOW")
    void shouldGetNoShowPatients() {
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{1L, "Ana", "Torres", "11111111", 2L});

        when(appointmentRepository.findNoShowPatients(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(AppointmentStatus.NO_SHOW)))
                .thenReturn(mockResult);

        Page<NoShowPatientResponse> result = reportService.getNoShowPatients(from, to, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).patientId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).patientName()).isEqualTo("Ana Torres");
        assertThat(result.getContent().get(0).documentNumber()).isEqualTo("11111111");
        assertThat(result.getContent().get(0).noShowCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getOfficeOccupancy - debe retornar página vacía cuando no hay datos")
    void shouldReturnEmptyPageWhenNoOfficeOccupancyData() {
        List<Object[]> mockResult = new ArrayList<>();

        when(appointmentRepository.findOfficeOccupancy(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(AppointmentStatus.CANCELLED)))
                .thenReturn(mockResult);

        Page<OfficeOccupancyResponse> result = reportService.getOfficeOccupancy(from, to, pageable);

        assertThat(result).isEmpty();
    }
}