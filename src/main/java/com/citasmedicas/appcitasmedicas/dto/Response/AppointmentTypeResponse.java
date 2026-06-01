package com.citasmedicas.appcitasmedicas.dto.Response;

import lombok.Builder;

// ==========================================
// 2. APPOINTMENT TYPE RESPONSE
// ==========================================
@Builder
public record AppointmentTypeResponse(
        Long id,
        String name,
        Integer durationMinutes,
        String description
) {}