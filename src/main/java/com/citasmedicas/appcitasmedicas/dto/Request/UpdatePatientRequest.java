package com.citasmedicas.appcitasmedicas.dto.Request;

import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;

public record UpdatePatientRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        PatientStatus status
) {}