package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import um.tesoreria.mercadopago.audit.AuditEventPublisher;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.EstadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.OrigenNotificacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ResultadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.ProcesarWebhookPagoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Toda notificación tiene que dejar evento, incluso cuando se rechaza.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PagoServiceTest {

    @Mock
    private ProcesarWebhookPagoUseCase procesarWebhookPagoUseCase;
    @Mock
    private RecuperarYPublicarPagoUseCase recuperarYPublicarPagoUseCase;
    @Mock
    private AuditEventPublisher audit;

    private PagoService service;

    @BeforeEach
    void setUp() {
        service = new PagoService(procesarWebhookPagoUseCase, recuperarYPublicarPagoUseCase, audit);
    }

    @Test
    void webhookProcesadoSeAuditaComoOk() {
        var cabeceras = new CabecerasWebhook("ts=1,v1=firma", "req-1");
        when(procesarWebhookPagoUseCase.procesar(cabeceras, "12345"))
                .thenReturn(new ResultadoWebhookPago(EstadoWebhookPago.PROCESADO, "ok"));

        var resultado = service.processPaymentWebhook(cabeceras, "12345");

        assertThat(resultado.exitoso()).isTrue();
        verify(audit).publishNotification(eq("12345"), eq("WEBHOOK_V1"), eq("PROCESADO"), eq(true),
                argThat(metadata -> Boolean.TRUE.equals(metadata.get("signaturePresent"))
                        && Boolean.TRUE.equals(metadata.get("requestIdPresent"))));
    }

    @Test
    void webhookConFirmaInvalidaSeAuditaComoError() {
        var cabeceras = new CabecerasWebhook("ts=1,v1=mala", "req-1");
        when(procesarWebhookPagoUseCase.procesar(cabeceras, "12345"))
                .thenReturn(new ResultadoWebhookPago(EstadoWebhookPago.FIRMA_INVALIDA, "Firma inválida"));

        service.processPaymentWebhook(cabeceras, "12345");

        verify(audit).publishNotification(eq("12345"), eq("WEBHOOK_V1"), eq("FIRMA_INVALIDA"), eq(false),
                argThat(metadata -> "Firma inválida".equals(metadata.get("message"))));
    }

    @Test
    void notificacionLegacyIgnoradaTambienSeAudita() {
        when(procesarWebhookPagoUseCase.procesarNotificacionLegada("99"))
                .thenReturn(new ResultadoWebhookPago(EstadoWebhookPago.LEGACY_IGNORADA, "ignorada"));

        var resultado = service.processLegacyPaymentNotification("99");

        assertThat(resultado.exitoso()).isFalse();
        verify(audit).publishNotification(eq("99"), eq("IPN_LEGACY"), eq("LEGACY_IGNORADA"), eq(false),
                anyMap());
    }

    @Test
    void replayManualSeAudita() {
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("77", OrigenNotificacion.MANUAL))
                .thenReturn(true);

        assertThat(service.retrieveAndPublishPayment("77")).isTrue();

        verify(audit).publishNotification(eq("77"), eq("MANUAL"), eq("PROCESADO"), eq(true), anyMap());
    }

    @Test
    void replayManualFallidoSeAuditaComoError() {
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("77", OrigenNotificacion.MANUAL))
                .thenReturn(false);

        assertThat(service.retrieveAndPublishPayment("77")).isFalse();

        verify(audit).publishNotification(eq("77"), eq("MANUAL"), eq("PAGO_NO_ENCONTRADO"), eq(false), anyMap());
    }

    @Test
    void replayManualConExcepcionSeAuditaYSePropaga() {
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("77", OrigenNotificacion.MANUAL))
                .thenThrow(new IllegalStateException("core caído"));

        assertThatThrownBy(() -> service.retrieveAndPublishPayment("77"))
                .isInstanceOf(IllegalStateException.class);

        verify(audit).publishNotification(eq("77"), eq("MANUAL"), eq("ERROR"), eq(false), any());
    }
}
