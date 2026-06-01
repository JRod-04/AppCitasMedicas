package com.citasmedicas.appcitasmedicas.dto.Response;

import lombok.Builder;

@Builder
public record OfficeOccupancyResponse(
        Long officeId,
        String officeName,
        Long appointmentCount
) {}
