package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import com.citasmedicas.appcitasmedicas.Entity.Specialty;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateDoctorRequest;
import com.citasmedicas.appcitasmedicas.Repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoctorRequestMapper {
    
    private final SpecialtyRepository specialtyRepository;
    
    public Doctor toEntity(CreateDoctorRequest request) {
        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new RuntimeException("Specialty not found"));
        
        return Doctor.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .licenseNumber(request.licenseNumber())
                .email(request.email())
                .specialty(specialty)
                .active(true)
                .build();
    }
    
    public void updateEntity(Doctor doctor, UpdateDoctorRequest request) {
        if (request.firstName() != null) {
            doctor.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            doctor.setLastName(request.lastName());
        }
        if (request.email() != null) {
            doctor.setEmail(request.email());
        }
        if (request.active() != null) {
            doctor.setActive(request.active());
        }
        if (request.specialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.specialtyId())
                    .orElseThrow(() -> new RuntimeException("Specialty not found"));
            doctor.setSpecialty(specialty);
        }
    }
}