package com.citasmedicas.appcitasmedicas.dto.Response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AvailabilitySlotResponse(
        LocalDateTime startAt,
        LocalDateTime endAt) {}
