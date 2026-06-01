package com.citasmedicas.appcitasmedicas.ControllerTests;


import com.citasmedicas.appcitasmedicas.Controller.SpecialtyController;
import com.citasmedicas.appcitasmedicas.Exception.GlobalExceptionHandler;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Service.SpecialtyService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateSpecialtyRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.SpecialtyResponse;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialtyController Tests")
class SpecialtyControllerTest {

    @Mock
    private SpecialtyService specialtyService;

    @InjectMocks
    private SpecialtyController specialtyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SpecialtyResponse specialtyResponse;
    private CreateSpecialtyRequest createRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(specialtyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        specialtyResponse = SpecialtyResponse.builder()
                .id(1L)
                .name("Cardiología")
                .description("Especialidad del corazón")
                .build();

        createRequest = new CreateSpecialtyRequest("Cardiología", "Especialidad del corazón");
    }

    @Test
    @DisplayName("create - debe crear una especialidad")
    void shouldCreate() throws Exception {
        when(specialtyService.create(any(CreateSpecialtyRequest.class))).thenReturn(specialtyResponse);

        mockMvc.perform(post("/api/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Cardiología"));
    }

    @Test
    @DisplayName("findAll - debe retornar página de especialidades")
    void shouldFindAll() throws Exception {
        Page<SpecialtyResponse> page = new PageImpl<>(
                List.of(specialtyResponse),
                PageRequest.of(0, 10),
                1
        );
        when(specialtyService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/specialties")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("delete - debe eliminar una especialidad")
    void shouldDeleteSpecialty() throws Exception {
        doNothing().when(specialtyService).delete(1L);

        mockMvc.perform(delete("/api/specialties/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete - debe retornar 404 cuando la especialidad no existe")
    void shouldReturnNotFoundWhenDeletingNonExistentSpecialty() throws Exception {
        doThrow(new ResourceNotFoundException("Specialty not found with id: 999"))
                .when(specialtyService).delete(999L);

        mockMvc.perform(delete("/api/specialties/999"))
                .andExpect(status().isNotFound());
    }
}