package com.citasmedicas.appcitasmedicas.dto.Response;

import lombok.Builder;

@Builder
public record NoShowPatientResponse(
        Long patientId,
        String patientName,
        String documentNumber,
        Long noShowCount
) {}
