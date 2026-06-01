package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorResponse;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {
    public DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .firstName(d.getFirstName())
                .lastName(d.getLastName())
                .licenseNumber(d.getLicenseNumber())
                .email(d.getEmail())
                .active(d.isActive())
                .specialtyId(d.getSpecialty().getId())
                .specialtyName(d.getSpecialty().getName())
                .build();
    }
}
