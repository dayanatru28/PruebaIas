package com.bancoias.preapproved.dto;

import com.bancoias.preapproved.model.PreApprovedStatus;

import java.math.BigDecimal;

//Estructura de la informacion de los pre aprobados
public record PreApprovedDto(
    String id,
    String customerId,
    PreApprovedStatus status,
    BigDecimal availableAmount
) {
}
