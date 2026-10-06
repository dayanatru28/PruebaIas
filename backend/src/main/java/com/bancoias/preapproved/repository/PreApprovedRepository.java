package com.bancoias.preapproved.repository;

import com.bancoias.preapproved.entity.PreApproved;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

//Conexion entre la tabla de pre aprobados y las busquedas que necesito realizar
@Repository
public interface PreApprovedRepository extends ReactiveCrudRepository<PreApproved, String> {

    //Consulto todos los preaprobados por usuario
    Flux<PreApproved> findByCustomerId(String customerId);

    //PARTE CLAVE DEL PROCESO 
    //ESTA FUNCION ME IMPIDE QUE SE HAGAN DOS O MAS DESCUENTOS SI SE HACEN MUCHAS SOLCITUDES Y AL FINAL EL VALOR DEL MONTO DISPONIBLE ES SUPERADO

    //Se creo dado a que la base de datos, me permite si o si una insercion por tiempo, entonces si coloco las condiciones de los montos,
    //al momento de realizar la nueva insercion, el sistema se ve obligado a hacer de nuevo las validaciones correspondientes.
    //Por eso, si la segunda insercion supera el monto, no me hace ningun cambios y no ejecuta el descuento

    //Edito el valor del preaprobado con las condiciones solicitadas
    @Modifying
    @Query("UPDATE pre_approved SET available_amount = available_amount - :amount, version = version + 1 " +
           "WHERE id = :id AND available_amount >= :amount AND status = 'ACTIVE'")
    Mono<Integer> decrementAvailableAmount(String id, BigDecimal amount);
}
