package com.citasmedicas.appcitasmedicas.Exception;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class AppointmentValidator {

    // Valida que la fecha del turno no sea en el pasado
    public void validateFutureDateTime(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Appointment date must be in the future");
        }
    }

    // Valida que el rango de fechas sea válido
    public void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("Start date must be before end date");
        }
    }

    // Valida que el horario del schedule sea válido
    public void validateScheduleTime(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException("Start time must be before end time");
        }
    }
}
