package com.citasmedicas.appcitasmedicas.Controller;


import com.citasmedicas.appcitasmedicas.Service.DoctorScheduleService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorScheduleRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorScheduleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/doctors/{doctorId}/schedules")
@RequiredArgsConstructor
@Validated
public class DoctorScheduleController {

    private final DoctorScheduleService service;

    @PostMapping
    public ResponseEntity<DoctorScheduleResponse> create(
            @PathVariable Long doctorId,
            @Valid @RequestBody CreateDoctorScheduleRequest req,
            UriComponentsBuilder uriBuilder) {
        var created = service.create(doctorId, req);
        var location = uriBuilder.path("/api/doctors/{doctorId}/schedules/{id}")
                .buildAndExpand(doctorId, created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<DoctorScheduleResponse>> findByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = service.findByDoctor(doctorId, PageRequest.of(page, size, Sort.by("id").ascending()));
        return ResponseEntity.ok(result);
    }
}
