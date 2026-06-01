package com.citasmedicas.appcitasmedicas.Controller;



import com.citasmedicas.appcitasmedicas.Service.AvailabilityService;
import com.citasmedicas.appcitasmedicas.dto.Response.AvailabilitySlotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Validated
public class AvailabilityController {

    private final AvailabilityService service;

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<Page<AvailabilitySlotResponse>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long appointmentTypeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = service.getAvailableSlots(doctorId, date, appointmentTypeId,
                PageRequest.of(page, size, Sort.by("id").ascending()));
        return ResponseEntity.ok(result);
    }
}
