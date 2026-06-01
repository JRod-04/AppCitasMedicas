package com.citasmedicas.appcitasmedicas.dto.Request;

import jakarta.validation.constraints.NotBlank;

public record CancelAppointmentRequest(
        @NotBlank(message = "El motivo de la cancelación es obligatorio")
        String cancellationReason
) {}
