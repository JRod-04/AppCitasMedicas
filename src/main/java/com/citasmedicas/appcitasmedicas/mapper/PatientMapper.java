package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.Patient;
import com.citasmedicas.appcitasmedicas.dto.Response.PatientResponse;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .documentNumber(p.getDocumentNumber())
                .email(p.getEmail())
                .phone(p.getPhone())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
