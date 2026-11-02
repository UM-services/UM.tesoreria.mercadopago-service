package um.tesoreria.mercadopago.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de la auditoría de mercadopago.
 *
 * <p>Este servicio no tiene base de datos propia, así que no puede usar un outbox
 * transaccional: los eventos viven en una cola acotada en memoria y se envían por lotes.
 * Si el proceso muere con eventos en vuelo, se pierden; por eso el nivel mínimo es
 * {@code REQUEST} y la decisión está documentada en PLANIFICACION_AUDITORIA_TESORERIA.md.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "tesoreria.audit")
public class AuditProperties {

    public enum Mode {
        /** No captura nada. */
        OFF,
        /** Captura y loguea, sin enviar. */
        LOG,
        /** Encola y envía al servicio de auditoría. */
        SEND
    }

    private Mode mode = Mode.SEND;

    private boolean requestEvents = true;

    private boolean changeEvents = true;

    /** Auditar también lecturas (GET). En mercadopago no hace falta: no expone PII masiva. */
    private boolean accessEvents = false;

    private String url = "http://localhost:8211";

    private String apiKey = "";

    /**
     * Tope de la cola en memoria: al llenarse se descartan eventos y se cuenta.
     *
     * <p>Dimensionado para la creación masiva de preferencias (un evento por preferencia):
     * {@code batch-size} × cada 2 s da ~100 eventos/s de drenaje, así que 5.000 absorben una
     * corrida de chequeras completa sin perder eventos.</p>
     */
    private int queueCapacity = 5000;

    private int batchSize = 200;

    private int maxAttempts = 10;

    private long senderDelayMs = 2000;

    private long senderInitialDelayMs = 5000;

    /** Sólo se auditan paths que empiecen con alguno de estos prefijos. */
    private List<String> includePrefixes = new ArrayList<>(List.of("/api/tesoreria/mercadopago"));

    /** Fragments de path que nunca se auditan (health, error). */
    private List<String> excludeFragments = new ArrayList<>(List.of("/actuator", "/error"));
}
