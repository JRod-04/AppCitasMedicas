package com.citasmedicas.appcitasmedicas.Controller;


import com.citasmedicas.appcitasmedicas.Service.DoctorService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateDoctorRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.DoctorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {

    private final DoctorService service;

    @PostMapping
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest req,
                                                 UriComponentsBuilder uriBuilder) {
        var created = service.create(req);
        var location = uriBuilder.path("/api/doctors/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> findAll(
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        if (specialtyId != null) {
            return ResponseEntity.ok(service.findBySpecialty(specialtyId, pageable));
        }
        return ResponseEntity.ok(service.findAll(pageable));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @RequestBody UpdateDoctorRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }
}
