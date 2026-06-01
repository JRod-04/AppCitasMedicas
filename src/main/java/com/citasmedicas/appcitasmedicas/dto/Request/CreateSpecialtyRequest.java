package com.citasmedicas.appcitasmedicas.dto.Request;

import jakarta.validation.constraints.NotBlank;

public record CreateSpecialtyRequest(
        @NotBlank(message = "El nombre de la especialidad es obligatorio")
        String name,
        String description

){}
