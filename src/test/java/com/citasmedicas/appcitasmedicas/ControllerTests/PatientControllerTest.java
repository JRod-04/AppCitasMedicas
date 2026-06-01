package com.citasmedicas.appcitasmedicas.ControllerTests;

import com.citasmedicas.appcitasmedicas.Controller.PatientController;
import com.citasmedicas.appcitasmedicas.Exception.GlobalExceptionHandler;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Service.PatientService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.PatientResponse;
import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
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
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientController Tests")
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PatientResponse patientResponse;
    private CreatePatientRequest createRequest;
    private UpdatePatientRequest updateRequest;

    @BeforeEach
    void setUp() {
        // ✅ Configurar ObjectMapper con soporte para JsonNullable
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JsonNullableModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ Configurar MockMvc con el ObjectMapper personalizado
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(patientController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();

        patientResponse = PatientResponse.builder()
                .id(1L)
                .firstName("Ana")
                .lastName("Torres")
                .email("ana@test.com")
                .phone("3001234567")
                .status(PatientStatus.ACTIVE)
                .build();

        createRequest = new CreatePatientRequest("Ana", "Torres", "11111111", "ana@test.com", "3001234567", PatientStatus.ACTIVE);

        updateRequest = new UpdatePatientRequest(
                JsonNullable.of("Ana Actualizada"),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        );
    }

    @Test
    @DisplayName("create - debe crear un paciente")
    void shouldCreate() throws Exception {
        when(patientService.create(any(CreatePatientRequest.class))).thenReturn(patientResponse);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("findById - debe retornar paciente por ID")
    void shouldFindById() throws Exception {
        when(patientService.findById(1L)).thenReturn(patientResponse);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"));
    }

    @Test
    @DisplayName("findAll - debe retornar página de pacientes")
    void shouldFindAll() throws Exception {
        Page<PatientResponse> page = new PageImpl<>(
                List.of(patientResponse),
                PageRequest.of(0, 10),
                1
        );
        when(patientService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/patients")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("update - debe actualizar un paciente")
    void shouldUpdate() throws Exception {
        // ✅ CORREGIDO: Usar patientService, no officeService
        when(patientService.update(eq(1L), any(UpdatePatientRequest.class))).thenReturn(patientResponse);

        // ✅ CORREGIDO: URL correcta para pacientes
        mockMvc.perform(patch("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("delete - debe eliminar un paciente")
    void shouldDeletePatient() throws Exception {
        doNothing().when(patientService).delete(1L);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete - debe retornar 404 cuando el paciente no existe")
    void shouldReturnNotFoundWhenDeletingNonExistentPatient() throws Exception {
        doThrow(new ResourceNotFoundException("Patient not found with id: 999"))
                .when(patientService).delete(999L);

        mockMvc.perform(delete("/api/patients/999"))
                .andExpect(status().isNotFound());
    }
}