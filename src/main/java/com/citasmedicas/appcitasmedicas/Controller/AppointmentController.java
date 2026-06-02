package com.citasmedicas.appcitasmedicas.Controller;



import com.citasmedicas.appcitasmedicas.Service.AppointmentService;
import com.citasmedicas.appcitasmedicas.dto.Request.CancelAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.CreateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdateAppointmentRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.AppointmentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Validated
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request,
                                                      UriComponentsBuilder uriBuilder) {
        var created = service.create(request);
        var location = uriBuilder.path("/api/appointments/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }


    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AppointmentResponse> result = service.findAll(pageable); // Pasa el objeto pageable al servicio
        return ResponseEntity.ok(result);
    }
    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirm(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id,
                                                      @Valid @RequestBody CancelAppointmentRequest request) {
        return ResponseEntity.ok(service.cancel(id, request ));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable Long id,
                                                        @RequestBody(required = false) Map<String, String> body) {
        String observations = body != null ? body.get("observations") : null;
        return ResponseEntity.ok(service.complete(id, observations));
    }

    @PutMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> noShow(@PathVariable Long id) {
        return ResponseEntity.ok(service.markNoShow(id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
public ResponseEntity<AppointmentResponse> update(
        @PathVariable Long id,
        @RequestBody UpdateAppointmentRequest request) {
    return ResponseEntity.ok(service.update(id, request));
}
}