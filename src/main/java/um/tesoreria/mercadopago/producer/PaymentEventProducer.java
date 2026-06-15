package um.tesoreria.mercadopago.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.domain.event.PaymentProcessedEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;
    private static final String TOPIC = "payment-processed";

    public void  publish(PaymentProcessedEvent event) {
        log.debug("\n\nPublishing payment event: {}\n\n", event.jsonify());
        kafkaTemplate.send(TOPIC, event.getPaymentId(), event);
    }
}
