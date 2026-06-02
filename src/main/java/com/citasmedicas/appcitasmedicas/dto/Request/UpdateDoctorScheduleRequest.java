package com.citasmedicas.appcitasmedicas.dto.Request;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record UpdateDoctorScheduleRequest(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}
