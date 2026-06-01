package com.citasmedicas.appcitasmedicas.dto.Request;

import org.openapitools.jackson.nullable.JsonNullable;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record UpdateDoctorScheduleRequest(
        JsonNullable<DayOfWeek> dayOfWeek,
        JsonNullable<LocalTime> startTime,
        JsonNullable<LocalTime> endTime
) {}
