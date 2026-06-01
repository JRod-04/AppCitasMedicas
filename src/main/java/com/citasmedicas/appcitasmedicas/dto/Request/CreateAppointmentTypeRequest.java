package com.citasmedicas.appcitasmedicas.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// ==========================================
// APPOINTMENT TYPE REQUEST
// ==========================================
public record CreateAppointmentTypeRequest(
        @NotBlank(message = "El nombre del tipo de cita es obligatorio")
        String name,

        @NotNull(message = "La duración es obligatoria")
        @Positive(message = "La duración debe ser mayor a 0 minutos")
        Integer durationMinutes,

        String description
) {}