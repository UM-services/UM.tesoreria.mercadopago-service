package um.tesoreria.mercadopago.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cola acotada de eventos pendientes de envío.
 *
 * <p>Sin outbox no hay garantía de entrega: si la cola se llena o el proceso muere, el evento
 * se pierde. Se elige así para no inventar una base de datos en un servicio que no tiene, y se
 * compensa con un contador {@code dropped} que se loguea y se puede exponer como métrica.</p>
 */
@Component
@Slf4j
public class AuditQueue {

    private final AuditProperties properties;
    private final BlockingQueue<QueuedEvent> events;
    private final AtomicLong dropped = new AtomicLong();

    public AuditQueue(AuditProperties properties) {
        this.properties = properties;
        this.events = new ArrayBlockingQueue<>(properties.getQueueCapacity());
    }

    public void offer(AuditEventPayload payload) {
        offer(new QueuedEvent(payload, 0));
    }

    void offer(QueuedEvent event) {
        if (!events.offer(event)) {
            long total = dropped.incrementAndGet();
            log.warn("Auditoría: cola llena, evento descartado (total descartados={})", total);
        }
    }

    public List<QueuedEvent> drain(int max) {
        List<QueuedEvent> batch = new ArrayList<>(max);
        events.drainTo(batch, max);
        return batch;
    }

    public int size() {
        return events.size();
    }

    public long dropped() {
        return dropped.get();
    }

    /** Espera a que se vacíe la cola (tests y shutdown ordenado). */
    public boolean awaitEmpty(long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (!events.isEmpty() && System.currentTimeMillis() < deadline) {
            TimeUnit.MILLISECONDS.sleep(50);
        }
        return events.isEmpty();
    }

    void clear() {
        events.clear();
    }

    /** Evento en cola con su contador de intentos de envío. */
    public record QueuedEvent(AuditEventPayload payload, int attempts) {

        QueuedEvent nextAttempt() {
            return new QueuedEvent(payload, attempts + 1);
        }
    }
}
