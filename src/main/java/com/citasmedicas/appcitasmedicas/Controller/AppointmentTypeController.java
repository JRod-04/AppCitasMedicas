package com.citasmedicas.appcitasmedicas.Controller;


import com.citasmedicas.appcitasmedicas.Service.AppointmentTypeService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentTypeRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentTypeResponse;
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
@RequestMapping("/api/appointment-types")
@RequiredArgsConstructor
@Validated
public class AppointmentTypeController {

    private final AppointmentTypeService service;

    @PostMapping
    public ResponseEntity<AppointmentTypeResponse> create(@Valid @RequestBody CreateAppointmentTypeRequest req,
                                                          UriComponentsBuilder uriBuilder) {
        var created = service.create(req);
        var location = uriBuilder.path("/api/appointment-types/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentTypeResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AppointmentTypeResponse> result = service.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        return ResponseEntity.ok(result);
    }
}

