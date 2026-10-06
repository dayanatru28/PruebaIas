package com.bancoias.preapproved.service;

import com.bancoias.preapproved.dto.UsageRequestDto;
import com.bancoias.preapproved.model.PreApprovedStatus;
import com.bancoias.preapproved.model.RequestStatus;
import com.bancoias.preapproved.repository.PreApprovedRepository;
import com.bancoias.preapproved.repository.UsageRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UsageRequestServiceIntegrationTest {

    @Autowired
    private UsageRequestService usageRequestService;

    @Autowired
    private PreApprovedRepository preApprovedRepository;

    @Autowired
    private UsageRequestRepository usageRequestRepository;

    @BeforeEach
    void setUp() {
        // Limpiamos solicitudes previas para garantizar pruebas aisladas e independientes
        usageRequestRepository.deleteAll().block();
        // Restauramos los cupos iniciales de prueba (Sección 3)
        preApprovedRepository.save(new com.bancoias.preapproved.entity.PreApproved(
                "PRA-1001", "USR-10", PreApprovedStatus.ACTIVE, new BigDecimal("1000000.00"), 0L)).block();
        preApprovedRepository.save(new com.bancoias.preapproved.entity.PreApproved(
                "PRA-1002", "USR-10", PreApprovedStatus.BLOCKED, new BigDecimal("800000.00"), 0L)).block();
        preApprovedRepository.save(new com.bancoias.preapproved.entity.PreApproved(
                "PRA-2001", "USR-20", PreApprovedStatus.ACTIVE, new BigDecimal("2000000.00"), 0L)).block();
    }

    @Test
    @DisplayName("RF01 & RF02: Solicitud válida debe ser AUTORIZADA y descontar del cupo disponible")
    void testSuccessfulUsageRequest_shouldAuthorizeAndDeduct() {
        UsageRequestDto dto = new UsageRequestDto("REF-TEST-001", "PRA-1001", "USR-10", new BigDecimal("600000.00"));

        StepVerifier.create(usageRequestService.processUsageRequest(dto))
            .assertNext(response -> {
                assertEquals(RequestStatus.AUTHORIZED, response.status());
                assertEquals("REF-TEST-001", response.requestReference());
                assertNull(response.rejectionReason());
                assertNotNull(response.processedAt());
            })
            .verifyComplete();

        // Verificar que el cupo en la BD quedó en 400.000 (1.000.000 - 600.000)
        StepVerifier.create(preApprovedRepository.findById("PRA-1001"))
            .assertNext(preApproved -> {
                assertEquals(0, new BigDecimal("400000.00").compareTo(preApproved.getAvailableAmount()));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("RF02 & RF03: Preaprobado BLOQUEADO debe ser RECHAZADO y NO descontar saldo")
    void testBlockedPreApproved_shouldRejectWithoutDeducting() {
        UsageRequestDto dto = new UsageRequestDto("REF-TEST-002", "PRA-1002", "USR-10", new BigDecimal("200000.00"));

        StepVerifier.create(usageRequestService.processUsageRequest(dto))
            .assertNext(response -> {
                assertEquals(RequestStatus.REJECTED, response.status());
                assertTrue(response.rejectionReason().toLowerCase().contains("bloqueado"));
            })
            .verifyComplete();

        // Verificar que el cupo en BD se mantiene intacto en 800.000
        StepVerifier.create(preApprovedRepository.findById("PRA-1002"))
            .assertNext(preApproved -> {
                assertEquals(0, new BigDecimal("800000.00").compareTo(preApproved.getAvailableAmount()));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("RF02: Monto superior al saldo disponible debe ser RECHAZADO")
    void testExceedingAmount_shouldReject() {
        UsageRequestDto dto = new UsageRequestDto("REF-TEST-003", "PRA-1001", "USR-10", new BigDecimal("1500000.00"));

        StepVerifier.create(usageRequestService.processUsageRequest(dto))
            .assertNext(response -> {
                assertEquals(RequestStatus.REJECTED, response.status());
                assertTrue(response.rejectionReason().toLowerCase().contains("supera el cupo"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("RF02: Cliente que no coincide con el dueño del preaprobado debe ser RECHAZADO")
    void testCustomerMismatch_shouldReject() {
        UsageRequestDto dto = new UsageRequestDto("REF-TEST-004", "PRA-1001", "USR-20", new BigDecimal("100000.00"));

        StepVerifier.create(usageRequestService.processUsageRequest(dto))
            .assertNext(response -> {
                assertEquals(RequestStatus.REJECTED, response.status());
                assertTrue(response.rejectionReason().toLowerCase().contains("no corresponde"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("RF05: Idempotencia - Referencia repetida no debe volver a descontar cupo")
    void testIdempotency_shouldPreserveOriginalAndNotDeductTwice() {
        UsageRequestDto dto = new UsageRequestDto("REF-IDEMP-001", "PRA-2001", "USR-20", new BigDecimal("500000.00"));

        // 1ra vez: debe autorizar y descontar 500.000 (saldo queda en 1.500.000)
        StepVerifier.create(usageRequestService.processUsageRequest(dto))
            .assertNext(response -> assertEquals(RequestStatus.AUTHORIZED, response.status()))
            .verifyComplete();

        // 2da vez con la MISMA referencia: debe retornar el resultado original SIN descontar otra vez
        StepVerifier.create(usageRequestService.processUsageRequest(dto))
            .assertNext(response -> {
                assertEquals(RequestStatus.AUTHORIZED, response.status());
                assertEquals("REF-IDEMP-001", response.requestReference());
            })
            .verifyComplete();

        // Verificar que el saldo quedó en 1.500.000 y NO en 1.000.000
        StepVerifier.create(preApprovedRepository.findById("PRA-2001"))
            .assertNext(preApproved -> {
                assertEquals(0, new BigDecimal("1500000.00").compareTo(preApproved.getAvailableAmount()));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("RF04: Concurrencia - Dos solicitudes simultáneas de 600.000 sobre saldo 1.000.000: Una autorizada y una rechazada")
    void testConcurrency_simultaneousRequestsMustNeverOverdraw() {
        UsageRequestDto req1 = new UsageRequestDto("REF-PARALLEL-1", "PRA-1001", "USR-10", new BigDecimal("600000.00"));
        UsageRequestDto req2 = new UsageRequestDto("REF-PARALLEL-2", "PRA-1001", "USR-10", new BigDecimal("600000.00"));

        // Ejecutamos ambas solicitudes en paralelo en hilos concurrentes separados
        Mono<List<com.bancoias.preapproved.dto.UsageResponseDto>> parallelExecution = Flux.merge(
                usageRequestService.processUsageRequest(req1).subscribeOn(Schedulers.parallel()),
                usageRequestService.processUsageRequest(req2).subscribeOn(Schedulers.parallel())
        ).collectList();

        StepVerifier.create(parallelExecution)
            .assertNext(responses -> {
                assertEquals(2, responses.size());
                long authorizedCount = responses.stream().filter(r -> r.status() == RequestStatus.AUTHORIZED).count();
                long rejectedCount = responses.stream().filter(r -> r.status() == RequestStatus.REJECTED).count();

                // Exactamente una debe haber sido autorizada y la otra rechazada
                assertEquals(1, authorizedCount, "Exactamente una solicitud debió ser autorizada");
                assertEquals(1, rejectedCount, "Exactamente una solicitud debió ser rechazada");
            })
            .verifyComplete();

        // Verificar que el saldo final es exactamente 400.000 (1.000.000 - 600.000) y NUNCA negativo
        StepVerifier.create(preApprovedRepository.findById("PRA-1001"))
            .assertNext(preApproved -> {
                assertEquals(0, new BigDecimal("400000.00").compareTo(preApproved.getAvailableAmount()),
                        "El saldo debe ser exactamente 400.000");
            })
            .verifyComplete();
    }
}
