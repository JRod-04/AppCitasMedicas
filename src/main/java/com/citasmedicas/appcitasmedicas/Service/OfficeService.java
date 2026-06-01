package com.citasmedicas.appcitasmedicas.Service;


import com.citasmedicas.appcitasmedicas.dto.Request.CreateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfficeService {
    OfficeResponse create(CreateOfficeRequest request);
    Page<OfficeResponse> findAll(Pageable page);
    OfficeResponse update(Long id, UpdateOfficeRequest request);
    void delete(Long id);
}

