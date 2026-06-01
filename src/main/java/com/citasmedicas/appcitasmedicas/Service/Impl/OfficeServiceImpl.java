package com.citasmedicas.appcitasmedicas.Service.Impl;



import com.citasmedicas.appcitasmedicas.Entity.Office;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.OfficeRepository;
import com.citasmedicas.appcitasmedicas.Service.OfficeService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeResponse;
import com.citasmedicas.appcitasmedicas.mapper.OfficeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;
    private final OfficeMapper officeMapper;

    @Override
    @Transactional
    public OfficeResponse create(CreateOfficeRequest request) {
        Office office = Office.builder()
                .name(request.name())
                .location(request.location())
                .floor(request.floor())
                .status(request.status())
                .build();
        return officeMapper.toResponse(officeRepository.save(office));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfficeResponse> findAll(Pageable pageable) {
        return officeRepository.findAll(pageable)
                .map(officeMapper::toResponse);
    }
    @Override
    @Transactional
    public OfficeResponse update(Long id, UpdateOfficeRequest request) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id: " + id));

        if (request.name().isPresent()) {
            office.setName(request.name().get());
        }
        if (request.location().isPresent()) {
            office.setLocation(request.location().get());
        }
        if (request.floor().isPresent()) {
            office.setFloor(request.floor().get());
        }
        if (request.status().isPresent()) {
            office.setStatus(request.status().get());
        }

        return officeMapper.toResponse(officeRepository.save(office));
    }
}
