package com.citasmedicas.appcitasmedicas.dto.Request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// ==========================================
// 1. APPOINTMENT REQUEST
// ==========================================
public record CreateAppointmentRequest(
        @NotNull(message = "El paciente es obligatorio")
        Long patientId,

        @NotNull(message = "El médico es obligatorio")
        Long doctorId,

        @NotNull(message = "El consultorio es obligatorio")
        Long officeId,

        @NotNull(message = "El tipo de cita es obligatorio")
        Long appointmentTypeId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime startAt,

        @NotNull(message = "La fecha de finalización es obligatoria")
        LocalDateTime endAt,

        String observations
) {}
