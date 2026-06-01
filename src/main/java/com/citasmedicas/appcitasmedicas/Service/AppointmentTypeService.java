package com.citasmedicas.appcitasmedicas.Service;


import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentTypeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentTypeService {
    AppointmentTypeResponse create(CreateAppointmentTypeRequest request);
    Page<AppointmentTypeResponse> findAll(Pageable page);
    void delete(Long id);
}

