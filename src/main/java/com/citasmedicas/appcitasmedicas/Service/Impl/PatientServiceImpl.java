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

    @Override
    @Transactional
    public PatientResponse create(CreatePatientRequest request) {
        patientRepository.findByDocumentNumber(request.documentNumber())
                .ifPresent(p -> {
                    throw new ConflictException("Patient with document number " + request.documentNumber() + " already exists");
                });

        Patient patient = Patient.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .documentNumber(request.documentNumber())
                .email(request.email())
                .phone(request.phone())
                .status(request.status())
                .build();

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse findById(Long id) {
        return patientMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> findAll(Pageable page) {
        return patientRepository.findAll(page)
                .map(patientMapper::toResponse);
    }

    @Override
    @Transactional
    public PatientResponse update(Long id, UpdatePatientRequest request) {
        Patient patient = getOrThrow(id);

        if (request.firstName().isPresent()) {
            patient.setFirstName(request.firstName().get());
        }
        if (request.lastName().isPresent()) {
            patient.setLastName(request.lastName().get());
        }
        if (request.email().isPresent()) {
            patient.setEmail(request.email().get());
        }
        if (request.phone().isPresent()) {
            patient.setPhone(request.phone().get());
        }
        if (request.status().isPresent()) {
            patient.setStatus(request.status().get());
        }

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    private Patient getOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }
}
