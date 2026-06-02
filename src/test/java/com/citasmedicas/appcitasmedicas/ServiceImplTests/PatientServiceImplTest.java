package com.citasmedicas.appcitasmedicas.ServiceImplTests;

import com.citasmedicas.appcitasmedicas.Entity.Patient;
import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import com.citasmedicas.appcitasmedicas.Exception.ConflictException;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.PatientRepository;
import com.citasmedicas.appcitasmedicas.Service.Impl.PatientServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Request.CreatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.PatientResponse;
import com.citasmedicas.appcitasmedicas.mapper.PatientMapper;
import com.citasmedicas.appcitasmedicas.mapper.PatientRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Tests")
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private PatientRequestMapper patientRequestMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient patient;
    private PatientResponse patientResponse;
    private CreatePatientRequest createRequest;
    private UpdatePatientRequest updateRequest;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .firstName("Ana")
                .lastName("Torres")
                .documentNumber("11111111")
                .email("ana@test.com")
                .phone("3001234567")
                .status(PatientStatus.ACTIVE)
                .build();

        patientResponse = PatientResponse.builder()
                .id(1L)
                .firstName("Ana")
                .lastName("Torres")
                .email("ana@test.com")
                .phone("3001234567")
                .status(PatientStatus.ACTIVE)
                .build();

        createRequest = new CreatePatientRequest(
                "Ana", "Torres", "11111111", "ana@test.com", "3001234567", PatientStatus.ACTIVE
        );

        // ✅ UpdateRequest AHORA ES JSON PLANO (sin JsonNullable)
        updateRequest = new UpdatePatientRequest(
                "Ana Actualizada",  // firstName
                null,               // lastName
                null,               // email
                null,               // phone
                null                // status
        );
    }

    @Test
    @DisplayName("create - debe crear un paciente exitosamente")
    void shouldCreatePatientSuccessfully() {
        when(patientRepository.findByDocumentNumber("11111111")).thenReturn(Optional.empty());
        when(patientRequestMapper.toEntity(any(CreatePatientRequest.class))).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

        PatientResponse result = patientService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Ana");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando el documento ya existe")
    void shouldThrowWhenDocumentNumberExists() {
        when(patientRepository.findByDocumentNumber("11111111")).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Patient with document number 11111111 already exists");
    }

    @Test
    @DisplayName("findById - debe encontrar un paciente por ID")
    void shouldFindById() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

        PatientResponse result = patientService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - debe lanzar excepción cuando el paciente no existe")
    void shouldThrowWhenPatientNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found with id: 1");
    }

    @Test
    @DisplayName("findAll - debe retornar página de pacientes")
    void shouldFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> patientPage = new PageImpl<>(List.of(patient));

        when(patientRepository.findAll(pageable)).thenReturn(patientPage);
        when(patientMapper.toResponse(any(Patient.class))).thenReturn(patientResponse);

        Page<PatientResponse> result = patientService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("update - debe actualizar un paciente exitosamente")
    void shouldUpdatePatient() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        doAnswer(invocation -> {
            Patient patientToUpdate = invocation.getArgument(0);
            UpdatePatientRequest req = invocation.getArgument(1);
            if (req.firstName() != null) {
                patientToUpdate.setFirstName(req.firstName());
            }
            return null;
        }).when(patientRequestMapper).updateEntity(any(Patient.class), any(UpdatePatientRequest.class));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

        PatientResponse result = patientService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(patient.getFirstName()).isEqualTo("Ana Actualizada");
    }
}