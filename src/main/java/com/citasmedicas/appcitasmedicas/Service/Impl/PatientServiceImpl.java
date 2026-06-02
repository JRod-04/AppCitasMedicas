package com.citasmedicas.appcitasmedicas.Service.Impl;

import com.citasmedicas.appcitasmedicas.Entity.Patient;
import com.citasmedicas.appcitasmedicas.Exception.ConflictException;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.PatientRepository;
import com.citasmedicas.appcitasmedicas.Service.PatientService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.PatientResponse;
import com.citasmedicas.appcitasmedicas.mapper.PatientMapper;
import com.citasmedicas.appcitasmedicas.mapper.PatientRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final PatientRequestMapper patientRequestMapper;

    @Override
    @Transactional
    public PatientResponse create(CreatePatientRequest req) {
        patientRepository.findByDocumentNumber(req.documentNumber())
                .ifPresent(p -> {
                    throw new ConflictException("Patient with document number " + req.documentNumber() + " already exists");
                });

        Patient patient = patientRequestMapper.toEntity(req);
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    public PatientResponse findById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return patientMapper.toResponse(patient);
    }

    @Override
    public Page<PatientResponse> findAll(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(patientMapper::toResponse);
    }

    @Override
    @Transactional
    public PatientResponse update(Long id, UpdatePatientRequest req) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        patientRequestMapper.updateEntity(patient, req);

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
    }
}