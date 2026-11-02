package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.ports.out.PublicadorPagoProcesado;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.event.PaymentProcessedEvent;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.mapper.PagoProcesadoMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducerAdapter implements PublicadorPagoProcesado {

    private static final String TOPIC = "payment-processed";

    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;
    private final PagoProcesadoMapper pagoProcesadoMapper;

    @Override
    public void publicar(PagoProcesadoEvento evento) {
        log.debug("\n\nPublishing payment event: {}\n\n", evento);
        PaymentProcessedEvent wire = pagoProcesadoMapper.toWire(evento);
        kafkaTemplate.send(TOPIC, wire.getPaymentId(), wire);
    }

}
