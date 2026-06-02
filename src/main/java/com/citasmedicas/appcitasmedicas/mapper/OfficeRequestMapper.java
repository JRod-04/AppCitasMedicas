package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.Office;
import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateOfficeRequest;
import org.springframework.stereotype.Component;

@Component
public class OfficeRequestMapper {
    
    public Office toEntity(CreateOfficeRequest request) {
        return Office.builder()
                .name(request.name())
                .location(request.location())
                .floor(request.floor())
                .status(request.status() != null ? request.status() : OfficeStatus.ACTIVE)
                .build();
    }
    
    public void updateEntity(Office office, UpdateOfficeRequest request) {
        if (request.name() != null) {
            office.setName(request.name());
        }
        if (request.location() != null) {
            office.setLocation(request.location());
        }
        if (request.floor() != null) {
            office.setFloor(request.floor());
        }
        if (request.status() != null) {
            office.setStatus(request.status());
        }
    }
}