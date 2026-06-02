package com.citasmedicas.appcitasmedicas.ServiceImplTests;

import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import com.citasmedicas.appcitasmedicas.Entity.Specialty;
import com.citasmedicas.appcitasmedicas.Exception.ResourceNotFoundException;
import com.citasmedicas.appcitasmedicas.Repository.DoctorRepository;
import com.citasmedicas.appcitasmedicas.Repository.SpecialtyRepository;
import com.citasmedicas.appcitasmedicas.Service.Impl.DoctorServiceImpl;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorResponse;
import com.citasmedicas.appcitasmedicas.mapper.DoctorMapper;
import com.citasmedicas.appcitasmedicas.mapper.DoctorRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@DisplayName("DoctorService Tests")
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private DoctorRequestMapper doctorRequestMapper;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Doctor doctor;
    private Specialty specialty;
    private DoctorResponse doctorResponse;
    private CreateDoctorRequest createRequest;
    private UpdateDoctorRequest updateRequest;

    @BeforeEach
    void setUp() {
        specialty = Specialty.builder().id(1L).name("Cardiología").build();

        doctor = Doctor.builder()
                .id(1L)
                .firstName("Pedro")
                .lastName("Gil")
                .licenseNumber("LIC-100")
                .email("pedro@test.com")
                .specialty(specialty)
                .active(true)
                .build();

        doctorResponse = DoctorResponse.builder()
                .id(1L)
                .firstName("Pedro")
                .lastName("Gil")
                .email("pedro@test.com")
                .active(true)
                .build();

        createRequest = new CreateDoctorRequest(
                "Pedro", "Gil", "LIC-100", "pedro@test.com", 1L
        );

        // ✅ UpdateRequest AHORA ES JSON PLANO (sin JsonNullable)
        updateRequest = new UpdateDoctorRequest(
                "Carlos",  // firstName
                null,      // lastName
                null,      // email
                null,      // specialtyId
                null       // active
        );
    }

    @Test
    @DisplayName("create - debe crear un doctor exitosamente")
    void shouldCreateDoctorSuccessfully() {
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));
        when(doctorRequestMapper.toEntity(any(CreateDoctorRequest.class))).thenReturn(doctor);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);

        DoctorResponse result = doctorService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Pedro");
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    @DisplayName("create - debe lanzar excepción cuando la especialidad no existe")
    void shouldThrowWhenSpecialtyNotFound() {
        when(specialtyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found: 1");
    }

    @Test
    @DisplayName("findById - debe encontrar un doctor por ID")
    void shouldFindById() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);

        DoctorResponse result = doctorService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - debe lanzar excepción cuando el doctor no existe")
    void shouldThrowWhenDoctorNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 1");
    }

    @Test
    @DisplayName("findAll - debe retornar lista de doctores")
    void shouldFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(doctor)));
        when(doctorMapper.toResponse(any(Doctor.class))).thenReturn(doctorResponse);

        var result = doctorService.findAll(pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findBySpecialty - debe retornar doctores por especialidad")
    void shouldFindBySpecialty() {
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.findBySpecialtyIdAndActiveTrue(1L, pageable)).thenReturn(List.of(doctor));
        when(doctorMapper.toResponse(any(Doctor.class))).thenReturn(doctorResponse);

        var result = doctorService.findBySpecialty(1L, pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("update - debe actualizar un doctor exitosamente")
    void shouldUpdateDoctor() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        doAnswer(invocation -> {
            Doctor doctorToUpdate = invocation.getArgument(0);
            UpdateDoctorRequest req = invocation.getArgument(1);
            if (req.firstName() != null) {
                doctorToUpdate.setFirstName(req.firstName());
            }
            return null;
        }).when(doctorRequestMapper).updateEntity(any(Doctor.class), any(UpdateDoctorRequest.class));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);

        DoctorResponse result = doctorService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(doctor.getFirstName()).isEqualTo("Carlos");
    }
}