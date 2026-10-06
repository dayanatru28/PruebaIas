package com.bancoias.preapproved.repository;

import com.bancoias.preapproved.entity.UsageRequest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Conexion entre la tabla de solicitudes y las busquedas 
@Repository
public interface UsageRequestRepository extends ReactiveCrudRepository<UsageRequest, Long> {

    //Me llama todas las solicudes procedas (El historial completo)
    Flux<UsageRequest> findAllByOrderByProcessedAtDesc();

    //Me llama o busca las solicitudes por una referencia en especifico
    Mono<UsageRequest> findByRequestReference(String requestReference);

}
