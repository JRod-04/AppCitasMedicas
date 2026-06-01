package com.citasmedicas.appcitasmedicas.Service.Impl;



import com.citasmedicas.appcitasmedicas.Entity.Specialty;
import com.citasmedicas.appcitasmedicas.Exception.ConflictException;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.SpecialtyRepository;
import com.citasmedicas.appcitasmedicas.Service.SpecialtyService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateSpecialtyRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.SpecialtyResponse;
import com.citasmedicas.appcitasmedicas.mapper.SpecialtyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Override
    @Transactional
    public SpecialtyResponse create(CreateSpecialtyRequest request) {
        specialtyRepository.findByNameIgnoreCase(request.name())
                .ifPresent(s -> { throw new ConflictException("Specialty already exists: " + request.name()); });

        Specialty specialty = Specialty.builder()
                .name(request.name())
                .description(request.description())
                .build();

        return specialtyMapper.toResponse(specialtyRepository.save(specialty));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialtyResponse> findAll(Pageable pageable) {
        return specialtyRepository.findAll(pageable)
                .map(specialtyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialtyResponse findById(Long id) {
        return specialtyMapper.toResponse(specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + id)));
    }
}

