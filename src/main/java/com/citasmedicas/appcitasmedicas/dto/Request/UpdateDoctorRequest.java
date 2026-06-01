package com.citasmedicas.appcitasmedicas.dto.Request;

import org.openapitools.jackson.nullable.JsonNullable;


public record UpdateDoctorRequest(
        JsonNullable<String> firstName,
        JsonNullable<String> lastName,
        JsonNullable<String> email,
        JsonNullable<Long> specialtyId,
        JsonNullable<Boolean> active
) {}