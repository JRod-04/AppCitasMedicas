package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.Office;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeResponse;
import org.springframework.stereotype.Component;

@Component
public class OfficeMapper {
    public OfficeResponse toResponse(Office o) {
        return OfficeResponse.builder()
                .id(o.getId())
                .name(o.getName())
                .location(o.getLocation())
                .floor(o.getFloor())
                .status(o.getStatus())
                .build();
    }
}
