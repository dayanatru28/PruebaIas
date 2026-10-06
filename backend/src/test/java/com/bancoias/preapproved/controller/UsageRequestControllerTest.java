package com.bancoias.preapproved.controller;

import com.bancoias.preapproved.dto.UsageRequestDto;
import com.bancoias.preapproved.dto.UsageResponseDto;
import com.bancoias.preapproved.model.RequestStatus;
import com.bancoias.preapproved.service.UsageRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(UsageRequestController.class)
class UsageRequestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UsageRequestService usageRequestService;

    @Test
    @DisplayName("POST /api/v1/requests: Debe procesar solicitud y responder HTTP 200 con el DTO")
    void testProcessRequestEndpoint() {
        UsageRequestDto requestDto = new UsageRequestDto("REF-WEB-001", "PRA-1001", "USR-10", new BigDecimal("500000.00"));
        UsageResponseDto responseDto = new UsageResponseDto("REF-WEB-001", "PRA-1001", "USR-10",
                new BigDecimal("500000.00"), RequestStatus.AUTHORIZED, null, LocalDateTime.now());

        Mockito.when(usageRequestService.processUsageRequest(any(UsageRequestDto.class)))
                .thenReturn(Mono.just(responseDto));

        webTestClient.post()
                .uri("/api/v1/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.requestReference").isEqualTo("REF-WEB-001")
                .jsonPath("$.status").isEqualTo("AUTHORIZED")
                .jsonPath("$.amount").isEqualTo(500000.00);
    }

    @Test
    @DisplayName("GET /api/v1/requests/{reference}: Debe consultar solicitud existente por referencia")
    void testGetByReferenceEndpoint() {
        UsageResponseDto responseDto = new UsageResponseDto("REF-WEB-001", "PRA-1001", "USR-10",
                new BigDecimal("500000.00"), RequestStatus.AUTHORIZED, null, LocalDateTime.now());

        Mockito.when(usageRequestService.findByReference("REF-WEB-001"))
                .thenReturn(Mono.just(responseDto));

        webTestClient.get()
                .uri("/api/v1/requests/REF-WEB-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.requestReference").isEqualTo("REF-WEB-001")
                .jsonPath("$.status").isEqualTo("AUTHORIZED");
    }

    @Test
    @DisplayName("GET /api/v1/requests: Debe listar solicitudes recientes")
    void testGetRecentRequestsEndpoint() {
        UsageResponseDto responseDto = new UsageResponseDto("REF-WEB-001", "PRA-1001", "USR-10",
                new BigDecimal("500000.00"), RequestStatus.AUTHORIZED, null, LocalDateTime.now());

        Mockito.when(usageRequestService.findRecentRequests())
                .thenReturn(Flux.just(responseDto));

        webTestClient.get()
                .uri("/api/v1/requests")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UsageResponseDto.class)
                .hasSize(1);
    }
}
