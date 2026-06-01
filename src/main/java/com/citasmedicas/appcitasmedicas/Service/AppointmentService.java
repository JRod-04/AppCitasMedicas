package com.citasmedicas.appcitasmedicas.Service;

import com.citasmedicas.appcitasmedicas.dto.Request.CancelAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AppointmentService {
    AppointmentResponse create(CreateAppointmentRequest request);
    AppointmentResponse findById(Long id);
    Page<AppointmentResponse> findAll(Pageable Page);
    AppointmentResponse confirm(Long id);
    AppointmentResponse cancel(Long id, CancelAppointmentRequest request);
    AppointmentResponse complete(Long id, String observations);
    AppointmentResponse markNoShow(Long id);
    void delete(Long id);
}
