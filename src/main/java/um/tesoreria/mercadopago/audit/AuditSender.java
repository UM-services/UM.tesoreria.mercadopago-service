package um.tesoreria.mercadopago.audit;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Envía por lote los eventos de la cola en memoria al servicio de auditoría.
 *
 * <p>Reintenta con backoff acotado hasta {@code max-attempts}; pasado ese punto descarta y
 * deja el error en el log (no hay outbox que pueda quedarse con la fila).</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditSender {

    private final AuditProperties properties;
    private final AuditQueue queue;
    private final ObjectMapper objectMapper;

    private RestClient restClient;

    @PostConstruct
    void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Scheduled(fixedDelayString = "${tesoreria.audit.sender-delay-ms:2000}",
            initialDelayString = "${tesoreria.audit.sender-initial-delay-ms:5000}")
    public void sendPending() {
        if (properties.getMode() != AuditProperties.Mode.SEND) {
            return;
        }
        if (isBlank(properties.getUrl()) || isBlank(properties.getApiKey())) {
            log.warn("Auditoría: URL o API key no configuradas; los eventos quedan en la cola");
            return;
        }

        List<AuditQueue.QueuedEvent> batch = queue.drain(properties.getBatchSize());
        if (batch.isEmpty()) {
            return;
        }
        send(batch);
    }

    void send(List<AuditQueue.QueuedEvent> batch) {
        String body = batch.stream()
                .map(event -> objectMapper.writeValueAsString(event.payload()))
                .collect(Collectors.joining(",", "{\"events\":[", "]}"));

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(properties.getUrl() + "/api/tesoreria/auditoria/evento/lote")
                    .header("X-API-Key", properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
            handleSuccess(batch, response.getBody());
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 400 || status == 401 || status == 403 || status == 422) {
                log.error("Auditoría: lote rechazado (HTTP {}); se descartan {} evento(s): {}",
                        status, batch.size(), truncate(e.getResponseBodyAsString()));
            } else {
                retry(batch, "HTTP " + status);
            }
        } catch (RuntimeException e) {
            retry(batch, truncate(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private void handleSuccess(List<AuditQueue.QueuedEvent> batch, String responseBody) {
        Set<Integer> rejected = rejectedIndexes(responseBody, batch.size());
        if (!rejected.isEmpty()) {
            log.error("Auditoría: {} evento(s) rechazados por el servicio: {}",
                    rejected.size(), responseBody);
        }
    }

    private Set<Integer> rejectedIndexes(String responseBody, int size) {
        Set<Integer> rejected = new HashSet<>();
        try {
            JsonNode root = responseBody == null ? null : objectMapper.readTree(responseBody);
            if (root == null || !root.has("rejected")) {
                return rejected;
            }
            for (JsonNode node : root.get("rejected")) {
                JsonNode indexNode = node.get("index");
                if (indexNode == null || !indexNode.isNumber()) {
                    continue;
                }
                int index = indexNode.asInt();
                if (index >= 0 && index < size) {
                    rejected.add(index);
                }
            }
        } catch (RuntimeException e) {
            log.warn("Auditoría: respuesta inesperada del servicio ({})", e.getMessage());
        }
        return rejected;
    }

    private void retry(List<AuditQueue.QueuedEvent> batch, String reason) {
        for (AuditQueue.QueuedEvent event : batch) {
            if (event.attempts() + 1 >= properties.getMaxAttempts()) {
                log.error("Auditoría: evento {} descartado tras {} intentos: {}",
                        event.payload().getEventId(), event.attempts() + 1, reason);
            } else {
                queue.offer(event.nextAttempt());
            }
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
