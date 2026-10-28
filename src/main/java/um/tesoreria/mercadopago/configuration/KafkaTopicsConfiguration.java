package um.tesoreria.mercadopago.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

/**
 * Topics de los que {@code tesoreria-mercadopago} es dueño.
 *
 * <ul>
 *   <li>{@code payment-processed}: lo produce este servicio; lo consumen {@code core} y
 *       {@code umhub}.</li>
 *   <li>{@code tesoreria-audit-evento}: topic de auditoría. Lo declara el productor (este
 *       servicio) con las 3 particiones que necesita el consumidor
 *       ({@code tesoreria-auditoria}, {@code concurrency=3}). Ver
 *       {@code PLANIFICACION_AUDITORIA_MERCADOPAGO_KAFKA.md}.</li>
 * </ul>
 *
 * <p>Spring Boot crea estos {@code NewTopic} al arrancar con el {@code KafkaAdmin}
 * auto-configurado; el réplication factor es ambiental ({@code app.kafka.topics.replicas}) y nunca
 * se aplica a un topic existente.</p>
 */
@Configuration
public class KafkaTopicsConfiguration {

    static final String AUDIT_TOPIC = "tesoreria-audit-evento";
    static final String AUDIT_RETENTION_MS = "604800000";        // 7 días
    static final String AUDIT_RETENTION_BYTES = "1073741824";    // 1 GiB por partición

    @Bean
    NewTopic paymentProcessedTopic(@Value("${app.kafka.topics.partitions:1}") int partitions,
                                   @Value("${app.kafka.topics.replicas:1}") int replicas) {
        return TopicBuilder.name("payment-processed")
                .partitions(partitions)
                .replicas(replicas)
                .config("min.insync.replicas", String.valueOf(Math.min(2, replicas)))
                .build();
    }

    /**
     * Topic de auditoría: la partición es parte del contrato con {@code tesoreria-auditoria}
     * (3 hilos de consumo), por eso no se deriva de {@code app.kafka.topics.partitions}.
     */
    @Bean
    NewTopic auditTopic(@Value("${tesoreria.audit.kafka-topic:" + AUDIT_TOPIC + "}") String topic,
                        @Value("${tesoreria.audit.kafka-topic-partitions:3}") int partitions,
                        @Value("${app.kafka.topics.replicas:1}") int replicas) {
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicas)
                .configs(Map.of(
                        "min.insync.replicas", String.valueOf(Math.min(2, replicas)),
                        "retention.ms", AUDIT_RETENTION_MS,
                        "retention.bytes", AUDIT_RETENTION_BYTES,
                        "cleanup.policy", "delete"))
                .build();
    }
}
