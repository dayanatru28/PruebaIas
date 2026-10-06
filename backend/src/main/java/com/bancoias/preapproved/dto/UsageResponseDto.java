package com.bancoias.preapproved.dto;

import com.bancoias.preapproved.model.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Respuesta cuando hace una busqueda o la insercion de la solciitud
public record UsageResponseDto(
    String requestReference,
    String preApprovedId,
    String customerId,
    BigDecimal amount,
    RequestStatus status,
    String rejectionReason,
    LocalDateTime processedAt
) {
}
