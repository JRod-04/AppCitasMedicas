package com.citasmedicas.appcitasmedicas.Service.Impl;

import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import com.citasmedicas.appcitasmedicas.Entity.Specialty;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.SpecialtyRepository;
import com.citasmedicas.appcitasmedicas.Service.DoctorService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorResponse;
import com.citasmedicas.appcitasmedicas.mapper.DoctorMapper;
import com.citasmedicas.appcitasmedicas.mapper.DoctorRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorMapper doctorMapper;
    private final DoctorRequestMapper doctorRequestMapper;

    @Override
    @Transactional
    public DoctorResponse create(CreateDoctorRequest req) {
        Specialty specialty = specialtyRepository.findById(req.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + req.specialtyId()));

        Doctor doctor = doctorRequestMapper.toEntity(req);
        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Override
    public DoctorResponse findById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return doctorMapper.toResponse(doctor);
    }

    @Override
    public List<DoctorResponse> findAll(Pageable pageable) {
        return doctorRepository.findAll(pageable).stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> findBySpecialty(Long specialtyId, Pageable pageable) {
        return doctorRepository.findBySpecialtyIdAndActiveTrue(specialtyId, pageable).stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorResponse update(Long id, UpdateDoctorRequest req) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        doctorRequestMapper.updateEntity(doctor, req);

        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + id);
        }
        doctorRepository.deleteById(id);
    }
}