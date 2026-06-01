package com.citasmedicas.appcitasmedicas.ControllerTests;

import com.citasmedicas.appcitasmedicas.Controller.OfficeController;
import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import com.citasmedicas.appcitasmedicas.Service.OfficeService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeResponse;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OfficeController Tests")
class OfficeControllerTest {

    @Mock
    private OfficeService officeService;

    @InjectMocks
    private OfficeController officeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private OfficeResponse officeResponse;
    private CreateOfficeRequest createRequest;
    private UpdateOfficeRequest updateRequest;

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
                .standaloneSetup(officeController)
                .setMessageConverters(converter)
                .build();

        officeResponse = OfficeResponse.builder()
                .id(1L)
                .name("Consultorio 101")
                .location("Piso 1")
                .floor("101")
                .status(OfficeStatus.ACTIVE)
                .build();

        createRequest = new CreateOfficeRequest("Consultorio 101", "Piso 1", "101", OfficeStatus.ACTIVE);

        updateRequest = new UpdateOfficeRequest(
                JsonNullable.of("Consultorio 101 Actualizado"),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        );
    }

    @Test
    @DisplayName("create - debe crear un consultorio")
    void shouldCreate() throws Exception {
        when(officeService.create(any(CreateOfficeRequest.class))).thenReturn(officeResponse);

        mockMvc.perform(post("/api/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("findAll - debe retornar página de consultorios")
    void shouldFindAll() throws Exception {
        Page<OfficeResponse> page = new PageImpl<>(
                List.of(officeResponse),
                PageRequest.of(0, 10),
                1
        );
        when(officeService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/offices")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("update - debe actualizar un consultorio")
    void shouldUpdate() throws Exception {
        when(officeService.update(eq(1L), any(UpdateOfficeRequest.class))).thenReturn(officeResponse);

        mockMvc.perform(patch("/api/offices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}