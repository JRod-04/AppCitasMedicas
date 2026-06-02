package com.citasmedicas.appcitasmedicas.dto.Request;

import java.time.LocalDateTime;

public record UpdateAppointmentRequest(
        Long officeId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String observations
) {}
