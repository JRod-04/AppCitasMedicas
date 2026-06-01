package com.citasmedicas.appcitasmedicas.Service.Impl;


import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import com.citasmedicas.appcitasmedicas.Repository.AppointmentRepository;
import com.citasmedicas.appcitasmedicas.Service.ReportService;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorProductivityResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.NoShowPatientResponse;
import com.citasmedicas.appcitasmedicas.dto.Response.OfficeOccupancyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OfficeOccupancyResponse> getOfficeOccupancy(LocalDate from, LocalDate to, Pageable pageable) {
        List<OfficeOccupancyResponse> allResponses = appointmentRepository
                .findOfficeOccupancy(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), AppointmentStatus.CANCELLED)
                .stream()
                .map(row -> new OfficeOccupancyResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();

        return applyPagination(allResponses, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorProductivityResponse> getDoctorProductivity(LocalDate from, LocalDate to, Pageable pageable) {
        List<DoctorProductivityResponse> allResponses = appointmentRepository
                .findDoctorProductivity(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), AppointmentStatus.COMPLETED)
                .stream()
                .map(row -> new DoctorProductivityResponse(
                        ((Number) row[0]).longValue(),
                        row[1] + " " + row[2],
                        (String) row[3],
                        ((Number) row[4]).longValue()
                ))
                .toList();

        return applyPagination(allResponses, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoShowPatientResponse> getNoShowPatients(LocalDate from, LocalDate to, Pageable pageable) {
        List<NoShowPatientResponse> allResponses = appointmentRepository
                .findNoShowPatients(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), AppointmentStatus.NO_SHOW)
                .stream()
                .map(row -> new NoShowPatientResponse(
                        ((Number) row[0]).longValue(),
                        row[1] + " " + row[2],
                        (String) row[3],
                        ((Number) row[4]).longValue()
                ))
                .toList();

        return applyPagination(allResponses, pageable);
    }

    private <T> Page<T> applyPagination(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start > list.size()) {
            return Page.empty(pageable);
        }

        List<T> pagedList = list.subList(start, end);
        return new PageImpl<>(pagedList, pageable, list.size());
    }
}