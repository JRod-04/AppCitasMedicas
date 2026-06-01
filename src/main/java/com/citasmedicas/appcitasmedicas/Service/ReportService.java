package com.citasmedicas.appcitasmedicas.Service;

import com.citasmedicas.appcitasmedicas.dto.Response.DoctorProductivityResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.NoShowPatientResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeOccupancyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    Page<OfficeOccupancyResponse> getOfficeOccupancy(LocalDate from, LocalDate to, Pageable page);
    Page<DoctorProductivityResponse> getDoctorProductivity(LocalDate from, LocalDate to, Pageable page );
    Page<NoShowPatientResponse> getNoShowPatients(LocalDate from, LocalDate to, Pageable page);
}
