package um.tesoreria.mercadopago.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Arma el contexto de auditoría (actor, IP, correlación) y emite el evento REQUEST al
 * terminar la operación, incluso si falló.
 *
 * <p>El webhook de Mercado Pago llega sin identidad de usuario: queda como {@code ANONYMOUS}
 * con {@code actor_trust=CLIENT}. Los headers de actor los pone el frontend/gateway.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class AuditRequestFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ID_LEGACY = "X-Usuario-Id";
    private static final String HEADER_LOGIN = "X-Usuario-Login";
    private static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    private final AuditProperties properties;
    private final AuditQueue queue;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (properties.getMode() == AuditProperties.Mode.OFF) {
            return true;
        }
        String path = path(request);
        if (properties.getExcludeFragments().stream().anyMatch(path::contains)) {
            return true;
        }
        if (properties.getIncludePrefixes().stream().noneMatch(path::startsWith)) {
            return true;
        }
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        if (MUTATING.contains(method)) {
            return false;
        }
        return !(properties.isAccessEvents() && "GET".equals(method));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        AuditContext.Data data = new AuditContext.Data(
                correlationId(request),
                actorId(request),
                header(request, HEADER_LOGIN),
                clientIp(request),
                request.getMethod(),
                path(request));
        AuditContext.set(data);

        int status = HttpServletResponse.SC_OK;
        Throwable error = null;
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus() == 0 ? HttpServletResponse.SC_OK : response.getStatus();
        } catch (ServletException | IOException e) {
            error = e;
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            throw e;
        } catch (RuntimeException e) {
            error = e;
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            throw e;
        } finally {
            try {
                emit(request, status, error);
            } catch (RuntimeException e) {
                log.warn("No se pudo registrar el evento REQUEST de auditoría: {}", e.getMessage());
            }
            AuditContext.clear();
        }
    }

    private void emit(HttpServletRequest request, int status, Throwable error) {
        if (!properties.isRequestEvents()) {
            return;
        }
        String outcome = error != null || status >= 400 ? "ERROR" : "OK";
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("durationMs", System.currentTimeMillis() - AuditContext.startedAtMillis());
        metadata.put("path", path(request));
        if (error != null) {
            metadata.put("error", error.getClass().getSimpleName());
        }
        if (AuditContext.get() != null && AuditContext.get().getCorrelationId() != null) {
            metadata.put("correlationId", AuditContext.get().getCorrelationId());
        }

        AuditEventPayload payload = AuditEventPayload.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                .correlationId(AuditContext.correlationId())
                .service(AuditEventPublisher.SERVICE)
                .level("REQUEST")
                .operation(operation(request.getMethod()))
                .actorType(AuditContext.actorType())
                .actorId(AuditContext.actorId())
                .actorLogin(AuditContext.actorLogin())
                .actorTrust(AuditContext.actorTrust())
                .clientIp(AuditContext.clientIp())
                .httpMethod(request.getMethod())
                .endpoint(path(request))
                .outcome(outcome)
                .httpStatus(status)
                .affectedRows(0)
                .metadata(metadata)
                .build();

        if (properties.getMode() == AuditProperties.Mode.SEND) {
            queue.offer(payload);
        } else {
            log.info("auditoría (modo LOG): {} {} -> {}", request.getMethod(), path(request), status);
        }
    }

    private String correlationId(HttpServletRequest request) {
        String header = header(request, HEADER_REQUEST_ID);
        return header == null || header.isBlank() ? UUID.randomUUID().toString() : header;
    }

    private Long actorId(HttpServletRequest request) {
        String header = header(request, HEADER_USER_ID);
        if (header == null || header.isBlank()) {
            header = header(request, HEADER_USER_ID_LEGACY);
        }
        if (header == null || header.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(header.trim());
        } catch (NumberFormatException e) {
            log.debug("Header de usuario inválido: {}", header);
            return null;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = header(request, HEADER_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String header(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    private String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return context == null || context.isEmpty() ? uri : uri.substring(context.length());
    }

    private String operation(String method) {
        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST" -> "INSERT";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "ACCESS";
        };
    }
}
