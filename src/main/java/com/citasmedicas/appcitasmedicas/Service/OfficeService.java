package com.citasmedicas.appcitasmedicas.Service;

import com.citasmedicas.appcitasmedicas.dto.Request.CreateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfficeService {
    
    OfficeResponse create(CreateOfficeRequest req);
    
    OfficeResponse update(Long id, UpdateOfficeRequest req);
    
    OfficeResponse findById(Long id);
    
    Page<OfficeResponse> findAll(Pageable pageable);
    
    void delete(Long id);
}