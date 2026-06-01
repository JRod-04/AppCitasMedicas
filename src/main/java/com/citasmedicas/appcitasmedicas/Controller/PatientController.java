package com.citasmedicas.appcitasmedicas.Controller;



import com.citasmedicas.appcitasmedicas.Service.PatientService;
import com.citasmedicas.appcitasmedicas.dto.Request.CreatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Request.UpdatePatientRequest;
import com.citasmedicas.appcitasmedicas.dto.Response.PatientResponse;
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
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Validated
public class PatientController {

    private final PatientService service;

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest req,
                                                  UriComponentsBuilder uriBuilder) {
        var created = service.create(req);
        var location = uriBuilder.path("/api/patients/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = service.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatientResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdatePatientRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
}
