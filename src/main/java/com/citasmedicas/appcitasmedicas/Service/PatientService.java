package com.citasmedicas.appcitasmedicas.Service;

import com.citasmedicas.appcitasmedicas.dto.Request.CreatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PatientService {
    PatientResponse create(CreatePatientRequest request);
    PatientResponse findById(Long id);
    Page<PatientResponse> findAll(Pageable page);
    PatientResponse update(Long id, UpdatePatientRequest request);
    void delete(Long id);
}
