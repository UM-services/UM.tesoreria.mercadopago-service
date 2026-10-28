package um.tesoreria.mercadopago.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditRequestFilterTest {

    private static final FilterChain NOOP = (request, response) -> {
    };

    private AuditProperties properties;
    private AuditQueue queue;
    private AuditRequestFilter filter;

    @BeforeEach
    void setUp() {
        properties = new AuditProperties();
        properties.setMode(AuditProperties.Mode.SEND);
        properties.setApiKey("test-key");
        properties.setAccessEvents(false);
        queue = new AuditQueue(properties);
        filter = new AuditRequestFilter(properties, queue);
    }

    @Test
    void postDentroDelPrefijoEmiteUnEventoRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
                "/api/tesoreria/mercadopago/payment/listener");
        request.addHeader("X-User-Id", "42");
        request.addHeader("X-Usuario-Login", "dquinteros");
        request.addHeader("X-Request-Id", "corr-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, NOOP);

        List<AuditQueue.QueuedEvent> events = queue.drain(10);
        assertThat(events).hasSize(1);
        AuditEventPayload payload = events.getFirst().payload();
        assertThat(payload.getService()).isEqualTo("tesoreria-mercadopago");
        assertThat(payload.getLevel()).isEqualTo("REQUEST");
        assertThat(payload.getOperation()).isEqualTo("INSERT");
        assertThat(payload.getActorId()).isEqualTo(42L);
        assertThat(payload.getActorLogin()).isEqualTo("dquinteros");
        assertThat(payload.getActorType()).isEqualTo("USER");
        assertThat(payload.getActorTrust()).isEqualTo("CLIENT");
        assertThat(payload.getCorrelationId()).isEqualTo("corr-1");
        assertThat(payload.getEndpoint()).isEqualTo("/api/tesoreria/mercadopago/payment/listener");
        assertThat(payload.getOutcome()).isEqualTo("OK");
    }

    @Test
    void getNoSeAuditaSiAccessEventsEstaApagado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/api/tesoreria/mercadopago/payment/update/1");

        filter.doFilter(request, new MockHttpServletResponse(), NOOP);

        assertThat(queue.drain(10)).isEmpty();
    }

    @Test
    void pathFueraDelPrefijoNoSeAudita() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/haberes/core/algo");

        filter.doFilter(request, new MockHttpServletResponse(), NOOP);

        assertThat(queue.drain(10)).isEmpty();
    }

    @Test
    void modoOffNoCapturaNada() throws Exception {
        properties.setMode(AuditProperties.Mode.OFF);
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
                "/api/tesoreria/mercadopago/payment/listener");

        filter.doFilter(request, new MockHttpServletResponse(), NOOP);

        assertThat(queue.drain(10)).isEmpty();
    }

    @Test
    void errorHttpSeRegistraComoOutcomeError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
                "/api/tesoreria/mercadopago/payment/listener");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        filter.doFilter(request, response, NOOP);

        assertThat(queue.drain(10).getFirst().payload().getOutcome()).isEqualTo("ERROR");
    }
}
