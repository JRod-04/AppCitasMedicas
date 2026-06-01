package com.citasmedicas.appcitasmedicas.ControllerTests;

import com.citasmedicas.appcitasmedicas.Controller.AvailabilityController;
import com.citasmedicas.appcitasmedicas.Service.AvailabilityService;
import com.citasmedicas.appcitasmedicas.dto.Response.AvailabilitySlotResponse;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilityController Tests")
class AvailabilityControllerTest {

    @Mock
    private AvailabilityService availabilityService;

    @InjectMocks
    private AvailabilityController availabilityController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AvailabilitySlotResponse slotResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(availabilityController)
                .build();

        objectMapper = new ObjectMapper();

        slotResponse = new AvailabilitySlotResponse(
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(30)
        );
    }

    @Test
    @DisplayName("getAvailableSlots - debe retornar slots disponibles")
    void shouldGetAvailableSlots() throws Exception {
        Page<AvailabilitySlotResponse> page = new PageImpl<>(
                List.of(slotResponse),
                PageRequest.of(0, 10),
                1
        );
        when(availabilityService.getAvailableSlots(anyLong(), any(LocalDate.class), anyLong(), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/availability/doctors/1")
                        .param("date", "2024-12-31")
                        .param("appointmentTypeId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}