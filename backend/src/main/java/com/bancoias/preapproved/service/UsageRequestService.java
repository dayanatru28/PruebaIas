package com.bancoias.preapproved.service;

import com.bancoias.preapproved.dto.UsageRequestDto;
import com.bancoias.preapproved.dto.UsageResponseDto;
import com.bancoias.preapproved.entity.UsageRequest;
import com.bancoias.preapproved.messaging.RabbitMqEventPublisher;
import com.bancoias.preapproved.model.PreApprovedStatus;
import com.bancoias.preapproved.model.RequestStatus;
import com.bancoias.preapproved.repository.PreApprovedRepository;
import com.bancoias.preapproved.repository.UsageRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Maneja la logica de la prueba mediante el codigo
@Service
public class UsageRequestService {

    private static final Logger log = LoggerFactory.getLogger(UsageRequestService.class);

    private final PreApprovedRepository preApprovedRepository;
    private final UsageRequestRepository usageRequestRepository;
    private final RabbitMqEventPublisher eventPublisher;

    public UsageRequestService(PreApprovedRepository preApprovedRepository,
            UsageRequestRepository usageRequestRepository,
            RabbitMqEventPublisher eventPublisher) {
        this.preApprovedRepository = preApprovedRepository;
        this.usageRequestRepository = usageRequestRepository;
        this.eventPublisher = eventPublisher;
    }

    //Mapeo de la entidad al DTO
    private UsageResponseDto toResponseDto(UsageRequest entity) {
        return new UsageResponseDto(
                entity.getRequestReference(),
                entity.getPreApprovedId(),
                entity.getCustomerId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getRejectionReason(),
                entity.getProcessedAt());
    }

    // Consultar una solicitud por referencia
    public Mono<UsageResponseDto> findByReference(String requestReference) {
        return usageRequestRepository.findByRequestReference(requestReference)
                .map(this::toResponseDto);
    }

    // Consulto todas las solicitudes procesadas
    public Flux<UsageResponseDto> findRecentRequests() {
        return usageRequestRepository.findAllByOrderByProcessedAtDesc()
                .map(this::toResponseDto);
    }

    //Importante.
    //Este codigo me permite la creacion de una solicitud 
    //Manejan las validaciones necesarias
    private Mono<UsageResponseDto> executeNewUsageRequest(UsageRequestDto dto) {
        LocalDateTime now = LocalDateTime.now();

        //Aqui se empieza con las validaciones mencionadas en la prueba (REGLAS DEL NEGOCIO)
        //Verificacion del monto
        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return saveRejectedRequest(dto, "El monto solicitado debe ser mayor que cero", now);
        }

        //Verifica que el pre aprobado sea un codigo existente
        return preApprovedRepository.findById(dto.preApprovedId())
                .flatMap(preApproved -> {
                    //Verifica que sea el cliente
                    if (!preApproved.getCustomerId().equals(dto.customerId())) {
                        return saveRejectedRequest(dto, "El preaprobado no corresponde al cliente informado", now);
                    }
                    //Verifica el estado del preaprobado
                    if (preApproved.getStatus() != PreApprovedStatus.ACTIVE) {
                        return saveRejectedRequest(dto, "El preaprobado se encuentra bloqueado o no habilitado", now);
                    }

                    //Verifica que el monto no supere el cupo disponible
                    if (dto.amount().compareTo(preApproved.getAvailableAmount()) > 0) {
                        return saveRejectedRequest(dto, "El monto solicitado supera el cupo disponible", now);
                    }

                    //Como ya se pasaron todas las validaciones en este punto se ejecuta el 
                    //Metodo para la reduccion del monto aprobado
                    return preApprovedRepository.decrementAvailableAmount(dto.preApprovedId(), dto.amount())
                        //ejecuta el descuento y me dice cuantas filas afecto
                            .flatMap(rowsUpdated -> {
                                if (rowsUpdated > 0) {
                                    // si hay descuento exitoso, entonces hace la creacion de dicha solicitud 
                                    UsageRequest authorizedRequest = new UsageRequest(
                                            null,
                                            dto.requestReference(),
                                            dto.preApprovedId(),
                                            dto.customerId(),
                                            dto.amount(),
                                            RequestStatus.AUTHORIZED,
                                            null,
                                            now);

                                    //Guarda o almacena la solciitud
                                    return usageRequestRepository.save(authorizedRequest)
                                            .doOnSuccess(saved -> {
                                                log.info("Solicitud {} AUTORIZADA exitosamente para el cliente {}",
                                                        saved.getRequestReference(), saved.getCustomerId());
                                                //Llama el evento de Rabbbit
                                                eventPublisher.publishAuthorizedUsage(saved);
                                            })
                                            .map(this::toResponseDto);
                                } else {
                                    // Si no hay cambios porque otra solicitud modifico o consumio el total
                                    log.warn("No se jecuto por concurrencia para el preaprobado {}. Saldo insuficiente.",
                                            dto.preApprovedId());
                                    //Si no la guarda entonces lo que hace es rechazarla, llamo el metodo de guardar informacion rechazada 
                                    return saveRejectedRequest(dto,
                                            "El monto solicitado supera el cupo disponible debido a solicitudes simultáneas",
                                            now);
                                }
                            });
                })
                // Si el preaprobado no fue encontrado en la base de datos
                .switchIfEmpty(
                        Mono.defer(() -> saveRejectedRequest(dto, "El preaprobado no existe en el sistema", now)));
    }

    //Procesa la solicitud de pre aprobado con las reglas
    //Dando solucion a la peticion de la prueba donde me dice que no puedo cambiar informacion de una misma requestReference
    @Transactional
    public Mono<UsageResponseDto> processUsageRequest(UsageRequestDto dto) {
        // Busca si ya existe una referencia de dicha solicitud para no hacer cambios o modificaciones en la misma, dado a que ya se realizo
        return usageRequestRepository.findByRequestReference(dto.requestReference())
                .flatMap(existingRequest -> {
                    log.info("Referencia repetida detectada: {}", dto.requestReference());
                    // Validar si los datos de la solicitud coinciden con la original procesada
                    boolean sameData = existingRequest.getPreApprovedId().equals(dto.preApprovedId())
                            && existingRequest.getCustomerId().equals(dto.customerId())
                            && existingRequest.getAmount().compareTo(dto.amount()) == 0;
                    //Si la solciitud llega con datos diferentes se deja la que estaba 
                    if (!sameData) {
                        log.warn("La referencia {} llegó con datos diferentes. Se preserva el registro original.",
                                dto.requestReference());
                    }

                    // En ambos casos, se preserva el resultado original y NO se descuenta
                    // nuevamente el cupo
                    return Mono.just(toResponseDto(existingRequest));
                })
                .switchIfEmpty(Mono.defer(() -> executeNewUsageRequest(dto)));
                //Si no es asi se crea una nueva
    }

    //Crea el metodo para guardar la solciitud rechazada
    private Mono<UsageResponseDto> saveRejectedRequest(UsageRequestDto dto, String reason, LocalDateTime now) {
        UsageRequest rejectedRequest = new UsageRequest(
                null,
                dto.requestReference(),
                dto.preApprovedId(),
                dto.customerId(),
                dto.amount(),
                RequestStatus.REJECTED,
                reason,
                now);

        return usageRequestRepository.save(rejectedRequest)
                .doOnSuccess(
                        saved -> log.info("Solicitud {} RECHAZADA. Motivo: {}", saved.getRequestReference(), reason))
                .map(this::toResponseDto);
    }
}
