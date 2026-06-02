package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.*;
import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentRequestMapper {
    
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    
    public Appointment toEntity(CreateAppointmentRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        Office office = officeRepository.findById(request.officeId())
                .orElseThrow(() -> new RuntimeException("Office not found"));
        
        AppointmentType appointmentType = appointmentTypeRepository.findById(request.appointmentTypeId())
                .orElseThrow(() -> new RuntimeException("AppointmentType not found"));
        
        return Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(request.startAt())
                .endAt(request.endAt())
                .status(AppointmentStatus.SCHEDULED)
                .observations(request.observations())
                .build();
    }
}