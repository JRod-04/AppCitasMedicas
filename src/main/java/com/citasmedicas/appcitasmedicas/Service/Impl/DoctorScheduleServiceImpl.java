package com.citasmedicas.appcitasmedicas.Service.Impl;


import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import com.citasmedicas.appcitasmedicas.Entity.DoctorSchedule;
import com.citasmedicas.appcitasmedicas.Exception.BusinessException;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.DoctorScheduleRepository;
import com.citasmedicas.appcitasmedicas.Service.DoctorScheduleService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorScheduleRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorScheduleResponse;
import com.citasmedicas.appcitasmedicas.mapper.DoctorScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleMapper doctorScheduleMapper;



    @Override
    public DoctorScheduleResponse create(Long doctorId, CreateDoctorScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (!doctor.isActive()) {
            throw new BusinessException("Cannot add schedule to an inactive doctor");
        }

        if (request.startTime().isAfter(request.endTime()) ||
                request.startTime().equals(request.endTime())) {
            throw new BusinessException("Start time must be before end time");
        }

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        return doctorScheduleMapper.toResponse(doctorScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorScheduleResponse> findByDoctor(Long doctorId, Pageable page) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }
        return doctorScheduleRepository.findByDoctorId(doctorId, page)
                .map(doctorScheduleMapper::toResponse);
    }
}
