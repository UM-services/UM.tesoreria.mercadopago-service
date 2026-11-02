package um.tesoreria.mercadopago.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Punto de entrada de todos los eventos de auditoría de mercadopago.
 *
 * <p>Hay tres familias:</p>
 * <ul>
 *   <li><b>Preferencias</b> ({@code CHANGE/INSERT|UPDATE|DELETE}): alta y cambios de una
 *       preferencia de pago, con el snapshot de los campos relevantes.</li>
 *   <li><b>Notificaciones</b> ({@code REQUEST/ACCESS}): toda notificación recibida de Mercado
 *       Pago, incluso las rechazadas por firma o por datos faltantes.</li>
 *   <li><b>Jobs</b> ({@code REQUEST/ACCESS}): ejecución de los procesos programados.</li>
 * </ul>
 *
 * <p>Nunca se guarda el JSON crudo de la preferencia ni datos de tarjeta: sólo el snapshot de
 * campos de negocio (ids, importe, vencimiento, init point) y el resultado de la notificación.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditEventPublisher {

    static final String SERVICE = "tesoreria-mercadopago";

    static final String ENTITY_PREFERENCE = "MercadoPagoPreference";
    static final String ENTITY_NOTIFICATION = "MercadoPagoNotification";

    private final AuditProperties properties;
    private final AuditQueue queue;

    // ---------------------------------------------------------------- preferencias

    public void publishPreferenceCreated(String preferenceId, Map<String, Object> after) {
        publishPreference("INSERT", preferenceId, List.of(), Map.of(), after, "OK", null);
    }

    public void publishPreferenceUpdated(String preferenceId, List<String> changedFields,
                                         Map<String, Object> before, Map<String, Object> after) {
        if (changedFields == null || changedFields.isEmpty()) {
            return;
        }
        publishPreference("UPDATE", preferenceId, changedFields, before, after, "OK", null);
    }

    public void publishPreferenceFailed(String operation, String preferenceId, String error) {
        publishPreference(operation, preferenceId, List.of(), Map.of(), Map.of(), "ERROR", error);
    }

    private void publishPreference(String operation, String preferenceId,
                                   List<String> changedFields,
                                   Map<String, Object> before, Map<String, Object> after,
                                   String outcome, String error) {
        if (properties.getMode() == AuditProperties.Mode.OFF || !properties.isChangeEvents()) {
            return;
        }
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("preferenceId", preferenceId);
        if (error != null) {
            metadata.put("error", error);
        }

        emit(AuditEventPayload.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                .correlationId(correlation())
                .service(SERVICE)
                .level("CHANGE")
                .operation(operation)
                .entity(ENTITY_PREFERENCE)
                .entityId(preferenceId)
                .actorType(AuditContext.actorType())
                .actorId(AuditContext.actorId())
                .actorLogin(AuditContext.actorLogin())
                .actorTrust(AuditContext.actorTrust())
                .clientIp(AuditContext.clientIp())
                .httpMethod(AuditContext.httpMethod())
                .endpoint(AuditContext.endpoint())
                .outcome(outcome)
                .affectedRows(outcome.equals("OK") ? 1 : 0)
                .changedFields(changedFields)
                .beforeData(before.isEmpty() ? null : before)
                .afterData(after.isEmpty() ? null : after)
                .metadata(metadata)
                .build());
    }

    // ---------------------------------------------------------------- notificaciones

    /**
     * Registra una notificación recibida (webhook v1, IPN legacy o replay manual).
     *
     * @param paymentId id de pago o {@code data.id} tal como llegó; puede ser nulo si el request
     *                  no lo traía.
     * @param origen    {@code WEBHOOK_V1}, {@code IPN_LEGACY} o {@code MANUAL}
     * @param estado    estado del procesamiento ({@code PROCESADO}, {@code FIRMA_INVALIDA}, …)
     * @param exitoso   true sólo si el pago se recuperó y publicó
     * @param metadata  detalles adicionales (headers presentes, mensaje)
     */
    public void publishNotification(String paymentId, String origen, String estado,
                                    boolean exitoso, Map<String, Object> metadata) {
        if (properties.getMode() == AuditProperties.Mode.OFF || !properties.isRequestEvents()) {
            return;
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("origin", origen);
        detail.put("state", estado);
        detail.put("paymentId", paymentId);
        if (metadata != null) {
            detail.putAll(metadata);
        }

        emit(AuditEventPayload.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                .correlationId(correlation())
                .service(SERVICE)
                .level("REQUEST")
                .operation("ACCESS")
                .entity(ENTITY_NOTIFICATION)
                .entityId(paymentId)
                .actorType(AuditContext.actorType())
                .actorId(AuditContext.actorId())
                .actorLogin(AuditContext.actorLogin())
                .actorTrust(AuditContext.actorTrust())
                .clientIp(AuditContext.clientIp())
                .httpMethod(AuditContext.httpMethod())
                .endpoint(AuditContext.endpoint())
                .outcome(exitoso ? "OK" : "ERROR")
                .affectedRows(0)
                .metadata(detail)
                .build());
    }

    // ---------------------------------------------------------------- jobs

    /**
     * Registra la ejecución de un job: nombre, duración y resultado.
     *
     * <p>Se emite como {@code REQUEST} (no {@code CHANGE}) porque no hay entidad de negocio ni
     * diff: lo que importa es que el proceso corrió, cuándo y cómo terminó.</p>
     */
    public void publishScheduled(String job, String outcome, long durationMs, Throwable error) {
        if (properties.getMode() == AuditProperties.Mode.OFF || !properties.isRequestEvents()) {
            return;
        }
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("job", job);
        metadata.put("durationMs", durationMs);
        if (error != null) {
            metadata.put("error", error.getClass().getSimpleName());
        }

        emit(AuditEventPayload.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                .correlationId(UUID.randomUUID().toString())
                .service(SERVICE)
                .level("REQUEST")
                .operation("ACCESS")
                .actorType("SYSTEM")
                .actorTrust("SYSTEM")
                .endpoint("scheduler:" + job)
                .outcome(outcome)
                .affectedRows(null)
                .metadata(metadata)
                .build());
    }

    // ---------------------------------------------------------------- interno

    private void emit(AuditEventPayload payload) {
        if (properties.getMode() == AuditProperties.Mode.SEND) {
            queue.offer(payload);
        } else {
            log.info("auditoría (modo LOG): {} {} {} -> {}",
                    payload.getLevel(), payload.getOperation(), payload.getEntity(),
                    payload.getOutcome());
        }
    }

    /** Si el evento nace dentro de un request, se correlaciona con su REQUEST. */
    private String correlation() {
        String current = AuditContext.correlationId();
        return current == null || current.isBlank() ? UUID.randomUUID().toString() : current;
    }
}
