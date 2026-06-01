package com.citasmedicas.appcitasmedicas.dto.Response;

import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import lombok.Builder;

// ==========================================
// 5. OFFICE RESPONSE
// ==========================================
@Builder
public record OfficeResponse(
        Long id,
        String name,
        String location,
        String floor,
        OfficeStatus status
) {}
