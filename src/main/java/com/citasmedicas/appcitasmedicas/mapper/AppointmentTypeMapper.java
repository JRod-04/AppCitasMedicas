package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.AppointmentType;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentTypeResponse;
import org.springframework.stereotype.Component;

@Component
public class AppointmentTypeMapper {
    public AppointmentTypeResponse toResponse(AppointmentType at) {
        return AppointmentTypeResponse.builder()
                .id(at.getId())
                .name(at.getName())
                .durationMinutes(at.getDurationMinutes())
                .description(at.getDescription())
                .build();
    }
}
