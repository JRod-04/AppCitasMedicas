package com.citasmedicas.appcitasmedicas.ControllerTests;


import com.citasmedicas.appcitasmedicas.Controller.ReportController;
import com.citasmedicas.appcitasmedicas.Service.ReportService;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorProductivityResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.NoShowPatientResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeOccupancyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController Tests")
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private OfficeOccupancyResponse occupancyResponse;
    private DoctorProductivityResponse productivityResponse;
    private NoShowPatientResponse noShowResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(reportController)
                .build();

        objectMapper = new ObjectMapper();

        occupancyResponse = new OfficeOccupancyResponse(1L, "Consultorio 101", 10L);
        productivityResponse = new DoctorProductivityResponse(1L, "Pedro Gil", "Cardiología", 5L);
        noShowResponse = new NoShowPatientResponse(1L, "Ana Torres", "11111111", 2L);
    }

    @Test
    @DisplayName("officeOccupancy - debe retornar ocupación de consultorios")
    void shouldGetOfficeOccupancy() throws Exception {
        Page<OfficeOccupancyResponse> page = new PageImpl<>(
                List.of(occupancyResponse),
                PageRequest.of(0, 10),
                1
        );
        when(reportService.getOfficeOccupancy(any(LocalDate.class), any(LocalDate.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reports/office-occupancy")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("doctorProductivity - debe retornar productividad de doctores")
    void shouldGetDoctorProductivity() throws Exception {
        Page<DoctorProductivityResponse> page = new PageImpl<>(
                List.of(productivityResponse),
                PageRequest.of(0, 10),
                1
        );
        when(reportService.getDoctorProductivity(any(LocalDate.class), any(LocalDate.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reports/doctor-productivity")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("noShowPatients - debe retornar pacientes con NO_SHOW")
    void shouldGetNoShowPatients() throws Exception {
        Page<NoShowPatientResponse> page = new PageImpl<>(
                List.of(noShowResponse),
                PageRequest.of(0, 10),
                1
        );
        when(reportService.getNoShowPatients(any(LocalDate.class), any(LocalDate.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reports/no-show-patients")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}