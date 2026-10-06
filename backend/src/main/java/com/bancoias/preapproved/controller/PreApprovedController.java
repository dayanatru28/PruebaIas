package com.bancoias.preapproved.controller;

import com.bancoias.preapproved.dto.PreApprovedDto;
import com.bancoias.preapproved.service.PreApprovedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/preapproved")
public class PreApprovedController {

    private final PreApprovedService preApprovedService;

    public PreApprovedController(PreApprovedService preApprovedService) {
        this.preApprovedService = preApprovedService;
    }

    //Consulto todos los preaprobados
    @GetMapping
    public Flux<PreApprovedDto> getAllPreApproved() {
        return preApprovedService.findAll();
    }

    //Consulto por una persona en especifico
    @GetMapping("/customer/{customerId}")
    public Flux<PreApprovedDto> getByCustomerId(@PathVariable("customerId") String customerId) {
        return preApprovedService.findByCustomerId(customerId);
    }

    //Consulto por un preaprobado en especifico
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PreApprovedDto>> getById(@PathVariable("id") String id) {
        return preApprovedService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
