package com.citasmedicas.appcitasmedicas.dto.Request;

import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import org.openapitools.jackson.nullable.JsonNullable;

public record UpdateOfficeRequest(
        JsonNullable<String> name,
        JsonNullable<String> location,
        JsonNullable<String> floor,
        JsonNullable<OfficeStatus> status
) {}
