package com.citasmedicas.appcitasmedicas.dto.Response;

import lombok.Builder;

@Builder
public record SpecialtyResponse(
        Long id,
        String name,
        String description
) {}

