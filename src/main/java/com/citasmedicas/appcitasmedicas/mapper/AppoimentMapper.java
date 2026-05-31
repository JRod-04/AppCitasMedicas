package com.citasmedicas.appcitasmedicas.mapper;

import com.universidad.consultorio.dto.Response.AppointmentResponse;
import com.citasmedicas.appcitasmedicas.Entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName())
                .officeId(a.getOffice().getId())
                .officeName(a.getOffice().getName())
                .appointmentTypeId(a.getAppointmentType().getId())
                .appointmentTypeName(a.getAppointmentType().getName())
                .startAt(a.getStartAt())
                .endAt(a.getEndAt())
                .status(a.getStatus())
                .cancellationReason(a.getCancellationReason())
                .observations(a.getObservations())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
