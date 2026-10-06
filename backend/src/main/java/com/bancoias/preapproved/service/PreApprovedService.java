package com.bancoias.preapproved.service;

import com.bancoias.preapproved.dto.PreApprovedDto;
import com.bancoias.preapproved.entity.PreApproved;
import com.bancoias.preapproved.repository.PreApprovedRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class PreApprovedService {

    //Conexion a las consultas de la base de datos con las solcitudes recibadas del frontend
    //Maneja la logica del proyecto (en codigo)
    private final PreApprovedRepository preApprovedRepository;

    public PreApprovedService(PreApprovedRepository preApprovedRepository) {
        this.preApprovedRepository = preApprovedRepository;
    }

    private PreApprovedDto toDto(PreApproved entity) {
        return new PreApprovedDto(
            entity.getId(),
            entity.getCustomerId(),
            entity.getStatus(),
            entity.getAvailableAmount()
        );
    }

    //Pre aprobados completos
    public Flux<PreApprovedDto> findAll() {
        return preApprovedRepository.findAll().map(this::toDto);
    }

    //Consulta de los pre aprobados por cliente 
    public Flux<PreApprovedDto> findByCustomerId(String customerId) {
        return preApprovedRepository.findByCustomerId(customerId).map(this::toDto);
    }

    //Puede consultar directamente por el servicio aunquen no este creado desde que se utilice la misma gramatica
    //Consulto un preaprobado en especifico, por codigo
    public Mono<PreApprovedDto> findById(String id) {
        return preApprovedRepository.findById(id).map(this::toDto);
    }

    
}
