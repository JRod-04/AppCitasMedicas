package com.citasmedicas.appcitasmedicas.Repository;

import com.citasmedicas.appcitasmedicas.Entity.Office;
import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {
    List<Office> findByStatus(OfficeStatus status);
}
