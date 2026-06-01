package com.citasmedicas.appcitasmedicas.Service;


import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorScheduleRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface DoctorScheduleService {
    DoctorScheduleResponse create(Long doctorId, CreateDoctorScheduleRequest request);
    Page<DoctorScheduleResponse> findByDoctor(Long doctorId, Pageable page);
}

