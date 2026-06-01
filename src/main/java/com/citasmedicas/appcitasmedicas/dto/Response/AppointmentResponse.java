package com.citasmedicas.appcitasmedicas.dto.Response;

import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        Long doctorId,
        String doctorName,
        Long officeId,
        String officeName,
        Long appointmentTypeId,
        String appointmentTypeName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        AppointmentStatus status,
        String cancellationReason,
        String observations,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
