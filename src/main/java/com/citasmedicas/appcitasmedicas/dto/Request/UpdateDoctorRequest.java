package com.citasmedicas.appcitasmedicas.dto.Request;

public record UpdateDoctorRequest(
        String firstName,
        String lastName,
        String email,
        Long specialtyId,
        Boolean active
) {}