package com.citasmedicas.appcitasmedicas.Repository;

import com.citasmedicas.appcitasmedicas.Entity.Doctor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByActiveTrue();
    List<Doctor> findBySpecialtyIdAndActiveTrue(Long specialtyId, Pageable page);
}
