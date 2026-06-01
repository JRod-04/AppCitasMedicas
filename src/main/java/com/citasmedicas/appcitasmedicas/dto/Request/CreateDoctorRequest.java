package com.citasmedicas.appcitasmedicas.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// ==========================================
// 3. DOCTOR REQUEST
// ==========================================
public record CreateDoctorRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        String lastName,

        @NotBlank(message = "El número de licencia es obligatorio")
        String licenseNumber,

        @Email(message = "El formato del correo es inválido")
        String email,

        @NotNull(message = "La especialidad es obligatoria")
        Long specialtyId
) {}
