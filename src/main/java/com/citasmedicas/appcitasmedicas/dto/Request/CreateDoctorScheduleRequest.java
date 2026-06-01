package com.citasmedicas.appcitasmedicas.dto.Request;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

// ==========================================
// 4. DOCTOR SCHEDULE REQUEST
// ==========================================
public record CreateDoctorScheduleRequest(
        @NotNull(message = "El médico es obligatorio")
        Long doctorId,

        @NotNull(message = "El día de la semana es obligatorio")
        DayOfWeek dayOfWeek,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime endTime
) {}
