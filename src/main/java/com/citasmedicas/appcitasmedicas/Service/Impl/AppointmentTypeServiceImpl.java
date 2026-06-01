package com.citasmedicas.appcitasmedicas.Service.Impl;

import com.citasmedicas.appcitasmedicas.Entity.AppointmentType;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentTypeRepository;
import com.citasmedicas.appcitasmedicas.Service.AppointmentTypeService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentTypeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentTypeResponse;
import com.citasmedicas.appcitasmedicas.mapper.AppointmentTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentTypeServiceImpl implements AppointmentTypeService {

    private final AppointmentTypeRepository appointmentTypeRepository;
    private final AppointmentTypeMapper appointmentTypeMapper;

    @Override
    @Transactional
    public AppointmentTypeResponse create(CreateAppointmentTypeRequest request) {
        AppointmentType at = AppointmentType.builder()
                .name(request.name())
                .durationMinutes(request.durationMinutes())
                .description(request.description())
                .build();
        return appointmentTypeMapper.toResponse(appointmentTypeRepository.save(at));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentTypeResponse> findAll(Pageable pageable) {
        return appointmentTypeRepository.findAll(pageable)
                .map(appointmentTypeMapper::toResponse);
    }
}

