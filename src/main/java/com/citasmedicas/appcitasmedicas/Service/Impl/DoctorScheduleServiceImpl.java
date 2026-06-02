package com.citasmedicas.appcitasmedicas.Service.Impl;

import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import com.citasmedicas.appcitasmedicas.Entity.DoctorSchedule;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.DoctorScheduleRepository;
import com.citasmedicas.appcitasmedicas.Service.DoctorScheduleService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorScheduleRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateDoctorScheduleRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorScheduleResponse;
import com.citasmedicas.appcitasmedicas.mapper.DoctorScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleMapper doctorScheduleMapper;

    @Override
    @Transactional
    public DoctorScheduleResponse create(Long doctorId, CreateDoctorScheduleRequest req) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(req.dayOfWeek())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .build();

        return doctorScheduleMapper.toResponse(doctorScheduleRepository.save(schedule));
    }

    @Override
    public Page<DoctorScheduleResponse> findByDoctor(Long doctorId, Pageable pageable) {
        return doctorScheduleRepository.findByDoctorId(doctorId, pageable)
                .map(doctorScheduleMapper::toResponse);
    }

    @Override
    @Transactional
    public DoctorScheduleResponse update(Long id, UpdateDoctorScheduleRequest req) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));

        if (req.dayOfWeek() != null) {
            schedule.setDayOfWeek(req.dayOfWeek());
        }
        if (req.startTime() != null) {
            schedule.setStartTime(req.startTime());
        }
        if (req.endTime() != null) {
            schedule.setEndTime(req.endTime());
        }

        return doctorScheduleMapper.toResponse(doctorScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!doctorScheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found: " + id);
        }
        doctorScheduleRepository.deleteById(id);
    }
}