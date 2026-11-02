package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.audit.AuditEventPublisher;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.OrigenNotificacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ResultadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.ProcesarWebhookPagoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fachada de las notificaciones de pago. Además de delegar, deja auditoría de <b>todas</b> las
 * notificaciones recibidas, incluidas las rechazadas (firma inválida, data.id faltante,
 * notificación legacy ignorada) y los replays manuales.
 *
 * <p>El evento es {@code REQUEST/ACCESS} con {@code entity=MercadoPagoNotification}: no es un
 * cambio de datos, es el registro de que nos notificaron algo y qué hicimos con eso.</p>
 */
@Service
@RequiredArgsConstructor
public class PagoService {

    static final String ORIGEN_WEBHOOK = OrigenNotificacion.WEBHOOK_V1.name();
    static final String ORIGEN_LEGACY = OrigenNotificacion.IPN_LEGACY.name();
    static final String ORIGEN_MANUAL = OrigenNotificacion.MANUAL.name();

    private final ProcesarWebhookPagoUseCase procesarWebhookPagoUseCase;
    private final RecuperarYPublicarPagoUseCase recuperarYPublicarPagoUseCase;
    private final AuditEventPublisher audit;

    public ResultadoWebhookPago processPaymentWebhook(CabecerasWebhook cabeceras, String dataId) {
        var resultado = procesarWebhookPagoUseCase.procesar(cabeceras, dataId);
        audit.publishNotification(dataId, ORIGEN_WEBHOOK, resultado.estado().name(), resultado.exitoso(),
                detalle(resultado, cabeceras));
        return resultado;
    }

    public ResultadoWebhookPago processLegacyPaymentNotification(String paymentId) {
        var resultado = procesarWebhookPagoUseCase.procesarNotificacionLegada(paymentId);
        audit.publishNotification(paymentId, ORIGEN_LEGACY, resultado.estado().name(), resultado.exitoso(),
                detalle(resultado, null));
        return resultado;
    }

    public boolean retrieveAndPublishPayment(String dataId) {
        boolean exitoso;
        try {
            exitoso = recuperarYPublicarPagoUseCase.recuperarYPublicar(dataId, OrigenNotificacion.MANUAL);
        } catch (RuntimeException e) {
            audit.publishNotification(dataId, ORIGEN_MANUAL, "ERROR", false,
                    Map.of("error", e.getClass().getSimpleName()));
            throw e;
        }
        audit.publishNotification(dataId, ORIGEN_MANUAL, exitoso ? "PROCESADO" : "PAGO_NO_ENCONTRADO",
                exitoso, Map.of());
        return exitoso;
    }

    private Map<String, Object> detalle(ResultadoWebhookPago resultado, CabecerasWebhook cabeceras) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("message", resultado.mensaje());
        if (cabeceras != null) {
            metadata.put("signaturePresent", cabeceras.xSignature() != null && !cabeceras.xSignature().isBlank());
            metadata.put("requestIdPresent",
                    cabeceras.xRequestId() != null && !cabeceras.xRequestId().isBlank());
        }
        return metadata;
    }
}
