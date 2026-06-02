package com.citasmedicas.appcitasmedicas.Service.Impl;

import com.citasmedicas.appcitasmedicas.Entity.Office;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.OfficeRepository;
import com.citasmedicas.appcitasmedicas.Service.OfficeService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateOfficeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeResponse;
import com.citasmedicas.appcitasmedicas.mapper.OfficeMapper;
import com.citasmedicas.appcitasmedicas.mapper.OfficeRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;
    private final OfficeMapper officeMapper;
    private final OfficeRequestMapper officeRequestMapper;

    @Override
    @Transactional
    public OfficeResponse create(CreateOfficeRequest req) {
        Office office = officeRequestMapper.toEntity(req);
        return officeMapper.toResponse(officeRepository.save(office));
    }

    @Override
    public OfficeResponse findById(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id: " + id));
        return officeMapper.toResponse(office);
    }

    @Override
    public Page<OfficeResponse> findAll(Pageable pageable) {
        return officeRepository.findAll(pageable)
                .map(officeMapper::toResponse);
    }

    @Override
    @Transactional
    public OfficeResponse update(Long id, UpdateOfficeRequest req) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id: " + id));

        officeRequestMapper.updateEntity(office, req);

        return officeMapper.toResponse(officeRepository.save(office));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!officeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Office not found with id: " + id);
        }
        officeRepository.deleteById(id);
    }
}