package com.citasmedicas.appcitasmedicas.Service;

import com.citasmedicas.appcitasmedicas.dto.Request.CreateSpecialtyRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.SpecialtyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SpecialtyService {
    SpecialtyResponse create(CreateSpecialtyRequest request);
    Page<SpecialtyResponse> findAll(Pageable page);
    SpecialtyResponse findById(Long id);
    void delete(Long id);
}
