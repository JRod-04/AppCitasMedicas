package com.citasmedicas.appcitasmedicas.Service;

import com.citasmedicas.appcitasmedicas.dto.Request.CreatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {
    
    PatientResponse create(CreatePatientRequest req);
    
    PatientResponse update(Long id, UpdatePatientRequest req);
    
    PatientResponse findById(Long id);
    
    Page<PatientResponse> findAll(Pageable pageable);
    
    void delete(Long id);
}