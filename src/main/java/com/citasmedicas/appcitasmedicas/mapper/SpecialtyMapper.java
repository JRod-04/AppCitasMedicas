package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.Specialty;
import com.citasmedicas.appcitasmedicas.dto.Response.SpecialtyResponse;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyMapper {
    public SpecialtyResponse toResponse(Specialty s) {
        return SpecialtyResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .build();
    }
}
