package com.citasmedicas.appcitasmedicas.Service;


import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {
    DoctorResponse create(CreateDoctorRequest request);
    DoctorResponse findById(Long id);
    List<DoctorResponse> findAll(Pageable page);
    List<DoctorResponse> findBySpecialty(Long specialtyId, Pageable pageable);
    DoctorResponse update(Long id, UpdateDoctorRequest request);
}