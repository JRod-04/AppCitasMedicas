package com.citasmedicas.appcitasmedicas.dto.Request;

import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;

public record UpdateOfficeRequest(
        String name,
        String location,
        String floor,
        OfficeStatus status
) {}
