package com.citasmedicas.appcitasmedicas.dto.Response;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

// ==========================================
// 4. DOCTOR SCHEDULE RESPONSE
// ==========================================
@Builder
public record DoctorScheduleResponse(
        Long id,
        Long doctorId,
        String doctorName,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}
