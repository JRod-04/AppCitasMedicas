package com.citasmedicas.appcitasmedicas.Repository;

import com.citasmedicas.appcitasmedicas.entity.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {
}
