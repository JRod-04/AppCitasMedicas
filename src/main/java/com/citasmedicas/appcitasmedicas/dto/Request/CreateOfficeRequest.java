package com.citasmedicas.appcitasmedicas.dto.Request;

import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import jakarta.validation.constraints.NotBlank;

// ==========================================
// 5. OFFICE REQUEST
// ==========================================
public record CreateOfficeRequest(
        @NotBlank(message = "El nombre del consultorio es obligatorio")
        String name,
        String location,
        String floor,
        OfficeStatus status
) {}
