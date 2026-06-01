package com.citasmedicas.appcitasmedicas.dto.Request;

import com.citasmedicas.appcitasmedicas.Enums.PatientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// ==========================================
// 6. PATIENT REQUEST
// ==========================================
public  record CreatePatientRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        String lastName,

        @NotBlank(message = "El documento de identidad es obligatorio")
        String documentNumber,

        @Email(message = "El formato del correo es inválido")
        String email,

        String phone,

        PatientStatus status
) {}
