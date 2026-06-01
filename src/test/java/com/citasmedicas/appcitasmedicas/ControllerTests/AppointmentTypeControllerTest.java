package com.citasmedicas.appcitasmedicas.ControllerTests;


import com.citasmedicas.appcitasmedicas.Controller.AppointmentTypeController;
import com.citasmedicas.appcitasmedicas.Exception.GlobalExceptionHandler;
import com.citasmedicas.appcitasmedicas.Service.AppointmentTypeService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentTypeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentTypeResponse;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentTypeController Tests")
class AppointmentTypeControllerTest {

    @Mock
    private AppointmentTypeService appointmentTypeService;

    @InjectMocks
    private AppointmentTypeController appointmentTypeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AppointmentTypeResponse response;
    private CreateAppointmentTypeRequest request;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(appointmentTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        response = AppointmentTypeResponse.builder()
                .id(1L)
                .name("Consulta General")
                .durationMinutes(30)
                .description("Consulta médica general")
                .build();

        request = new CreateAppointmentTypeRequest("Consulta General", 30, "Consulta médica general");
    }

    @Test
    @DisplayName("create - debe crear un tipo de cita")
    void shouldCreate() throws Exception {
        when(appointmentTypeService.create(any(CreateAppointmentTypeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/appointment-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Consulta General"));
    }

    @Test
    @DisplayName("findAll - debe retornar página de tipos de cita")
    void shouldFindAll() throws Exception {
        Page<AppointmentTypeResponse> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );        when(appointmentTypeService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/appointment-types")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("delete - debe eliminar un tipo de cita")
    void shouldDeleteAppointmentType() throws Exception {
        doNothing().when(appointmentTypeService).delete(1L);

        mockMvc.perform(delete("/api/appointment-types/1"))
                .andExpect(status().isNoContent());
    }
}