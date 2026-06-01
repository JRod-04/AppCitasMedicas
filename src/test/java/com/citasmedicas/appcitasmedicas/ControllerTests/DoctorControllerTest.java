package com.citasmedicas.appcitasmedicas.ControllerTests;

import com.citasmedicas.appcitasmedicas.Controller.DoctorController;
import com.citasmedicas.appcitasmedicas.Service.DoctorService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorResponse;
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
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorController Tests")
class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DoctorResponse doctorResponse;
    private CreateDoctorRequest createRequest;
    private UpdateDoctorRequest updateRequest;

    @BeforeEach
    void setUp() {
        // ✅ Configurar ObjectMapper con soporte para JsonNullable y LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JsonNullableModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(doctorController)
                .setMessageConverters(converter)
                .build();

        doctorResponse = DoctorResponse.builder()
                .id(1L)
                .firstName("Pedro")
                .lastName("Gil")
                .email("pedro@test.com")
                .active(true)
                .build();

        createRequest = new CreateDoctorRequest("Pedro", "Gil", "LIC-100", "pedro@test.com", 1L);

        updateRequest = new UpdateDoctorRequest(
                org.openapitools.jackson.nullable.JsonNullable.of("Carlos"),
                org.openapitools.jackson.nullable.JsonNullable.undefined(),
                org.openapitools.jackson.nullable.JsonNullable.undefined(),
                org.openapitools.jackson.nullable.JsonNullable.undefined(),
                org.openapitools.jackson.nullable.JsonNullable.undefined()
        );
    }

    @Test
    @DisplayName("create - debe crear un doctor")
    void shouldCreate() throws Exception {
        when(doctorService.create(any(CreateDoctorRequest.class))).thenReturn(doctorResponse);

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("findById - debe retornar doctor por ID")
    void shouldFindById() throws Exception {
        when(doctorService.findById(1L)).thenReturn(doctorResponse);

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Pedro"));
    }

    @Test
    @DisplayName("findAll - debe retornar lista de doctores")
    void shouldFindAll() throws Exception {
        when(doctorService.findAll(any(PageRequest.class))).thenReturn(List.of(doctorResponse));

        mockMvc.perform(get("/api/doctors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("findAll - debe filtrar por especialidad")
    void shouldFindBySpecialty() throws Exception {
        when(doctorService.findBySpecialty(eq(1L), any(PageRequest.class))).thenReturn(List.of(doctorResponse));

        mockMvc.perform(get("/api/doctors")
                        .param("specialtyId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("updateDoctor - debe actualizar un doctor")
    void shouldUpdateDoctor() throws Exception {
        when(doctorService.update(eq(1L), any(UpdateDoctorRequest.class))).thenReturn(doctorResponse);

        mockMvc.perform(patch("/api/doctors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Pedro"));
    }
}