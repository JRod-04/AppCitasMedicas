package com.citasmedicas.appcitasmedicas.dto.Request;

import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import org.openapitools.jackson.nullable.JsonNullable;

public record UpdatePatientRequest(
        JsonNullable<String> firstName,
        JsonNullable<String> lastName,
        JsonNullable<String> email,
        JsonNullable<String> phone,
        JsonNullable<PatientStatus> status
) {}
