package com.bancoias.preapproved.controller;

import com.bancoias.preapproved.dto.UsageRequestDto;
import com.bancoias.preapproved.dto.UsageResponseDto;
import com.bancoias.preapproved.service.UsageRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/requests")
public class UsageRequestController {

    private final UsageRequestService usageRequestService;

    public UsageRequestController(UsageRequestService usageRequestService) {
        this.usageRequestService = usageRequestService;
    }

    //Creacion de solicitud
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<UsageResponseDto> processUsageRequest(@Valid @RequestBody UsageRequestDto requestDto) {
        return usageRequestService.processUsageRequest(requestDto);
    }

    //Consulto la informacion para un referencia en especifico
    @GetMapping("/{reference}")
    public Mono<ResponseEntity<UsageResponseDto>> getByReference(@PathVariable("reference") String reference) {
        return usageRequestService.findByReference(reference)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    //Consulto todas las solicitudes recientes
    @GetMapping
    public Flux<UsageResponseDto> getRecentRequests() {
        return usageRequestService.findRecentRequests();
    }
}
