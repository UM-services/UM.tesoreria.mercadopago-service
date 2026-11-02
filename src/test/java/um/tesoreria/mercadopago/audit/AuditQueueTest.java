package um.tesoreria.mercadopago.audit;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditQueueTest {

    private AuditQueue queue(int capacity) {
        AuditProperties properties = new AuditProperties();
        properties.setQueueCapacity(capacity);
        return new AuditQueue(properties);
    }

    @Test
    void descartaCuandoLaColaEstaLlena() {
        AuditQueue queue = queue(1);

        queue.offer(payload("uno"));
        queue.offer(payload("dos"));

        assertThat(queue.size()).isEqualTo(1);
        assertThat(queue.dropped()).isEqualTo(1);
        assertThat(queue.drain(10)).extracting(event -> event.payload().getEventId())
                .containsExactly("uno");
    }

    @Test
    void drainRespetaElMaximo() {
        AuditQueue queue = queue(10);
        for (int index = 0; index < 5; index++) {
            queue.offer(payload("e" + index));
        }

        List<AuditQueue.QueuedEvent> batch = queue.drain(2);

        assertThat(batch).hasSize(2);
        assertThat(queue.size()).isEqualTo(3);
    }

    private AuditEventPayload payload(String eventId) {
        return AuditEventPayload.builder()
                .eventId(eventId)
                .service("tesoreria-mercadopago")
                .level("REQUEST")
                .operation("ACCESS")
                .actorType("SYSTEM")
                .actorTrust("SYSTEM")
                .outcome("OK")
                .metadata(Map.of("path", "/api/tesoreria/mercadopago/payment/listener"))
                .build();
    }
}
