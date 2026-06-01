package com.citasmedicas.appcitasmedicas.dto.Response;

import lombok.Builder;

// ==========================================
// 3. DOCTOR RESPONSE
// ==========================================
@Builder
public record DoctorResponse(
        Long id,
        String firstName,
        String lastName,
        String licenseNumber,
        String email,
        boolean active,
        Long specialtyId,
        String specialtyName
) {}
