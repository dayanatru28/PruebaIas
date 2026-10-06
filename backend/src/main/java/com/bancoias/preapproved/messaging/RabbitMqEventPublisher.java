package com.bancoias.preapproved.messaging;

import com.bancoias.preapproved.entity.UsageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publicador opcional de eventos en RabbitMQ (Sección 9 del enunciado).
 * Diseñado con resiliencia: si RabbitMQ no está activo en el entorno local,
 * registra el evento en logs sin bloquear ni interrumpir la transacción de negocio.
 */
@Component
public class RabbitMqEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:bancoias.direct}")
    private String exchange;

    @Value("${rabbitmq.routing-key:preapproved.usage.authorized}")
    private String routingKey;

    public RabbitMqEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAuthorizedUsage(UsageRequest request) {
        try {
            String message = String.format(
                "{\"event\":\"PREAPPROVED_USAGE_AUTHORIZED\",\"reference\":\"%s\",\"preApprovedId\":\"%s\",\"customerId\":\"%s\",\"amount\":%s,\"processedAt\":\"%s\"}",
                request.getRequestReference(),
                request.getPreApprovedId(),
                request.getCustomerId(),
                request.getAmount(),
                request.getProcessedAt()
            );

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Evento publicado exitosamente en RabbitMQ [{}:{}]: {}", exchange, routingKey, message);
        } catch (Exception ex) {
            log.warn("RabbitMQ no está disponible en este momento. El evento no pudo publicarse pero la transacción fue autorizada: {}", ex.getMessage());
        }
    }
}
