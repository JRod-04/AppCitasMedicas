package com.citasmedicas.appcitasmedicas.ControllerTests;

import com.citasmedicas.appcitasmedicas.Controller.DoctorScheduleController;
import com.citasmedicas.appcitasmedicas.Service.DoctorScheduleService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorScheduleRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorScheduleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorScheduleController Tests")
class DoctorScheduleControllerTest {

    @Mock
    private DoctorScheduleService doctorScheduleService;

    @InjectMocks
    private DoctorScheduleController doctorScheduleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DoctorScheduleResponse scheduleResponse;
    private CreateDoctorScheduleRequest createRequest;

    @BeforeEach
    void setUp() {
        // ✅ Configurar ObjectMapper con soporte para LocalTime y DayOfWeek
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ Configurar MockMvc con el ObjectMapper personalizado
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(doctorScheduleController)
                .setMessageConverters(converter)
                .build();

        scheduleResponse = DoctorScheduleResponse.builder()
                .id(1L)
                .doctorId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        // ✅ Con doctorId incluido
        createRequest = new CreateDoctorScheduleRequest(
                1L,  // doctorId
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );
    }

    @Test
    @DisplayName("create - debe crear un horario")
    void shouldCreate() throws Exception {
        when(doctorScheduleService.create(eq(1L), any(CreateDoctorScheduleRequest.class))).thenReturn(scheduleResponse);

        mockMvc.perform(post("/api/doctors/1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("findByDoctor - debe retornar horarios del doctor")
    void shouldFindByDoctor() throws Exception {
        Page<DoctorScheduleResponse> page = new PageImpl<>(
                List.of(scheduleResponse),
                PageRequest.of(0, 10),
                1
        );
        when(doctorScheduleService.findByDoctor(eq(1L), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/doctors/1/schedules")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}