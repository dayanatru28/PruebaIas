package com.bancoias.preapproved.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

//El dto para recibir la informacion de la solcitud
public record UsageRequestDto(

    @NotBlank(message = "La referencia de la solicitud es obligatoria (codigo de factura)")
    String requestReference,

    @NotBlank(message = "El identificador del preaprobado es obligatorio")
    String preApprovedId,

    @NotBlank(message = "El identificador del cliente es obligatorio")
    String customerId,

    @NotNull(message = "El monto solicitado es obligatorio")
    //Aqui especifico cual es el valor minimo, si esta incluido este en el rando y el mensaje.
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto solicitado debe ser mayor que cero")
    BigDecimal amount
) {
}
