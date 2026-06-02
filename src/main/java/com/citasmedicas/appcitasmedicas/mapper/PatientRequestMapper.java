package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.Patient;
import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import com.citasmedicas.appcitasmedicas.dto.Request.CreatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdatePatientRequest;
import org.springframework.stereotype.Component;

@Component
public class PatientRequestMapper {
    
    public Patient toEntity(CreatePatientRequest request) {
        return Patient.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .documentNumber(request.documentNumber())
                .email(request.email())
                .phone(request.phone())
                .status(request.status() != null ? request.status() : PatientStatus.ACTIVE)
                .build();
    }
    
    public void updateEntity(Patient patient, UpdatePatientRequest request) {
        if (request.firstName() != null) {
            patient.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            patient.setLastName(request.lastName());
        }
        if (request.email() != null) {
            patient.setEmail(request.email());
        }
        if (request.phone() != null) {
            patient.setPhone(request.phone());
        }
        if (request.status() != null) {
            patient.setStatus(request.status());
        }
    }
}