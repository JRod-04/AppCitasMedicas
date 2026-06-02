package com.citasmedicas.appcitasmedicas.ControllerTests;



import com.citasmedicas.appcitasmedicas.Controller.AppointmentController;
import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.Service.AppointmentService;
import com.citasmedicas.appcitasmedicas.dto.Request.CancelAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentResponse;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentController Tests")
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AppointmentResponse appointmentResponse;
    private CreateAppointmentRequest createRequest;
    private CancelAppointmentRequest cancelRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ Configura MockMvc con el ObjectMapper que soporta LocalDateTime
        mockMvc = MockMvcBuilders
                .standaloneSetup(appointmentController)
                .build();


        appointmentResponse = AppointmentResponse.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(1L)
                .officeId(1L)
                .appointmentTypeId(1L)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusMinutes(30))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        createRequest = new CreateAppointmentRequest(
                1L, 1L, 1L, 1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusMinutes(30),
                null
        );

        cancelRequest = new CancelAppointmentRequest("Paciente no pudo asistir");
    }

    // ✅ Método auxiliar para convertir objeto a JSON con el ObjectMapper configurado
    private String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("create - debe crear una cita y retornar 201")
    void shouldCreateAppointment() throws Exception {
        when(appointmentService.create(any(CreateAppointmentRequest.class))).thenReturn(appointmentResponse);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("findById - debe retornar una cita por ID")
    void shouldFindById() throws Exception {
        when(appointmentService.findById(1L)).thenReturn(appointmentResponse);

        mockMvc.perform(get("/api/appointments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("findAll - debe retornar página de citas")
    void shouldFindAll() throws Exception {
        Page<AppointmentResponse> page =
                new PageImpl<>(
                        List.of(appointmentResponse),
                        PageRequest.of(0, 10),
                        1
                );
        when(appointmentService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/appointments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("confirm - debe confirmar una cita")
    void shouldConfirmAppointment() throws Exception {
        // ✅ CORREGIDO: Crear respuesta con status CONFIRMED
        AppointmentResponse confirmedResponse = AppointmentResponse.builder()
                .id(1L)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentService.confirm(1L)).thenReturn(confirmedResponse);

        mockMvc.perform(put("/api/appointments/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("cancel - debe cancelar una cita")
    void shouldCancelAppointment() throws Exception {
        AppointmentResponse cancelledResponse = AppointmentResponse.builder()
                .id(1L).status(AppointmentStatus.CANCELLED).build();
        when(appointmentService.cancel(eq(1L), any(CancelAppointmentRequest.class))).thenReturn(cancelledResponse);

        mockMvc.perform(put("/api/appointments/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("complete - debe completar una cita")
    void shouldCompleteAppointment() throws Exception {
        AppointmentResponse completedResponse = AppointmentResponse.builder()
                .id(1L).status(AppointmentStatus.COMPLETED).build();
        when(appointmentService.complete(eq(1L), any())).thenReturn(completedResponse);

        mockMvc.perform(put("/api/appointments/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observations\": \"Paciente atendido\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("noShow - debe marcar cita como NO_SHOW")
    void shouldMarkNoShow() throws Exception {
        AppointmentResponse noShowResponse = AppointmentResponse.builder()
                .id(1L).status(AppointmentStatus.NO_SHOW).build();
        when(appointmentService.markNoShow(1L)).thenReturn(noShowResponse);

        mockMvc.perform(put("/api/appointments/1/no-show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }
    @Test
    @DisplayName("delete - debe eliminar una cita")
    void shouldDeleteAppointment() throws Exception {
        // Given
        doNothing().when(appointmentService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/appointments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
@DisplayName("update - debe actualizar una cita")
void shouldUpdateAppointment() throws Exception {
    // Given
    UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
            null, 
            LocalDateTime.now().plusDays(2).withHour(11).withMinute(0),
            LocalDateTime.now().plusDays(2).withHour(11).withMinute(30),
            "Observación actualizada"
    );
    
    when(appointmentService.update(eq(1L), any(UpdateAppointmentRequest.class))).thenReturn(appointmentResponse);
    
    // When & Then
    mockMvc.perform(patch("/api/appointments/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
}
}