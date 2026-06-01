package com.citasmedicas.appcitasmedicas.mapper;

import com.citasmedicas.appcitasmedicas.Entity.DoctorSchedule;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorScheduleResponse;
import org.springframework.stereotype.Component;

@Component
public class DoctorScheduleMapper {
    public DoctorScheduleResponse toResponse(DoctorSchedule ds) {
        return DoctorScheduleResponse.builder()
                .id(ds.getId())
                .doctorId(ds.getDoctor().getId())
                .doctorName(ds.getDoctor().getFirstName() + " " + ds.getDoctor().getLastName())
                .dayOfWeek(ds.getDayOfWeek())
                .startTime(ds.getStartTime())
                .endTime(ds.getEndTime())
                .build();
    }
}