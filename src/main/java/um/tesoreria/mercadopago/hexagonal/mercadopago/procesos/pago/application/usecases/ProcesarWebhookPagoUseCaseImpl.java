package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ComponentesFirma;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.EstadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.FirmaWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.OrigenNotificacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ResultadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.ProcesarWebhookPagoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcesarWebhookPagoUseCaseImpl implements ProcesarWebhookPagoUseCase {

    private static final String PAGO_PROCESADO = "\n\nPayment processed\n\n";

    private final RecuperarYPublicarPagoUseCase recuperarYPublicarPagoUseCase;

    @Value("${app.secret-key:}")
    private String secretKey;

    /**
     * Obligatorio por defecto. Solo para entornos de prueba: con credenciales de testeo Mercado Pago
     * firma con un secret distinto al del panel y la validación nunca pasa.
     */
    @Value("${app.webhook.signature-validation:true}")
    private Boolean validarFirma;

    /**
     * Fallback IPN legacy ({@code ?id=<paymentId>&topic=payment}). Apagado por defecto: mientras
     * el webhook v1 y el legacy procesen el mismo pago en paralelo, los consumidores idempotentes
     * absorben el duplicado, pero no hay razón para habilitarlo fuera de un entorno controlado.
     */
    @Value("${app.webhook.legacy-ipn-enabled:false}")
    private Boolean legacyIpnHabilitado;

    @Override
    public ResultadoWebhookPago procesar(CabecerasWebhook cabeceras, String dataId) {
        if (dataId == null || dataId.isEmpty()) {
            log.error("Falta el parámetro data.id en la URL");
            return new ResultadoWebhookPago(EstadoWebhookPago.DATA_ID_FALTANTE,
                    "Falta el parámetro data.id en la URL");
        }

        log.debug("\n\nProcessing ProcesarWebhookPagoUseCaseImpl.procesar for {}\n\n", dataId);

        if (!firmaObligatoria()) {
            log.warn("Validación de firma de webhooks DESHABILITADA (app.webhook.signature-validation=false) "
                    + "para data.id={}: se procesa en modo permisivo, solo apto para entornos de prueba. "
                    + "No usar en producción.", dataId);
            trazarValidacionInformativa(cabeceras, dataId);
            return recuperarYPublicar(dataId, OrigenNotificacion.WEBHOOK_V1);
        }

        String xSignature = cabeceras.xSignature();
        String xRequestId = cabeceras.xRequestId();
        log.debug("Signature from MP -> {}", xSignature);

        if (xSignature == null || xSignature.isEmpty()) {
            log.warn("Falta el header x-signature para data.id={}", dataId);
            return new ResultadoWebhookPago(EstadoWebhookPago.FIRMA_FALTANTE, "Falta el header x-signature");
        }
        if (xRequestId == null || xRequestId.isEmpty()) {
            log.warn("Falta el header x-request-id para data.id={}", dataId);
            return new ResultadoWebhookPago(EstadoWebhookPago.REQUEST_ID_FALTANTE, "Falta el header x-request-id");
        }

        Optional<ComponentesFirma> componentes = Optional.ofNullable(ComponentesFirma.parse(xSignature));
        if (componentes.isEmpty()) {
            log.warn("Formato inválido en el header x-signature para data.id={}", dataId);
            return new ResultadoWebhookPago(EstadoWebhookPago.FIRMA_MAL_FORMADA,
                    "Formato inválido en el header x-signature");
        }

        String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, componentes.get().ts());
        log.debug("Manifest string: {}", manifest);

        if (secretKey == null || secretKey.isBlank()) {
            log.error("app.secret-key no configurada: no se puede validar la firma del webhook");
            return new ResultadoWebhookPago(EstadoWebhookPago.FIRMA_INVALIDA, "Firma inválida");
        }

        if (!FirmaWebhook.validar(secretKey, manifest, componentes.get().v1())) {
            log.error("Firma inválida para data.id={} (request-id={}, ts={}, v1={}): se descarta el webhook. "
                            + "app.secret-key en uso (huella SHA-256): {}",
                    dataId, xRequestId, componentes.get().ts(), prefijo(componentes.get().v1()), huella(secretKey));
            return new ResultadoWebhookPago(EstadoWebhookPago.FIRMA_INVALIDA, "Firma inválida");
        }

        return recuperarYPublicar(dataId, OrigenNotificacion.WEBHOOK_V1);
    }

    @Override
    public ResultadoWebhookPago procesarNotificacionLegada(String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) {
            log.error("Notificación legacy sin id de pago");
            return new ResultadoWebhookPago(EstadoWebhookPago.DATA_ID_FALTANTE,
                    "Falta el parámetro data.id en la URL");
        }

        log.debug("\n\nProcessing ProcesarWebhookPagoUseCaseImpl.procesarNotificacionLegada for {}\n\n", paymentId);

        if (!legacyHabilitada()) {
            log.info("Notificación IPN legacy ignorada para el pago {}: app.webhook.legacy-ipn-enabled=false. "
                    + "Se responde 200 para que Mercado Pago no reintente.", paymentId);
            return new ResultadoWebhookPago(EstadoWebhookPago.LEGACY_IGNORADA,
                    "Notificación legacy ignorada");
        }

        return recuperarYPublicar(paymentId, OrigenNotificacion.IPN_LEGACY);
    }

    private ResultadoWebhookPago recuperarYPublicar(String dataId, OrigenNotificacion origen) {
        try {
            if (!recuperarYPublicarPagoUseCase.recuperarYPublicar(dataId, origen)) {
                log.error("No se pudo recuperar el pago {}: no se publica el evento", dataId);
                return new ResultadoWebhookPago(EstadoWebhookPago.PAGO_NO_ENCONTRADO,
                        "No se pudo recuperar el pago desde Mercado Pago");
            }
            return new ResultadoWebhookPago(EstadoWebhookPago.PROCESADO, PAGO_PROCESADO);
        } catch (NumberFormatException e) {
            log.error("data.id no numérico: {}", dataId);
            return new ResultadoWebhookPago(EstadoWebhookPago.DATA_ID_INVALIDO, "Parámetro data.id inválido");
        }
    }

    private boolean legacyHabilitada() {
        return Boolean.TRUE.equals(legacyIpnHabilitado);
    }

    private boolean firmaObligatoria() {
        return validarFirma == null || validarFirma;
    }

    /**
     * Con la validación deshabilitada no se bloquea la notificación, pero se deja traza de si la firma
     * habría validado: sirve para detectar cuándo Mercado Pago empieza a firmar con el secret correcto.
     */
    private void trazarValidacionInformativa(CabecerasWebhook cabeceras, String dataId) {
        String xSignature = cabeceras.xSignature();
        String xRequestId = cabeceras.xRequestId();
        if (xSignature == null || xSignature.isEmpty() || xRequestId == null || xRequestId.isEmpty()) {
            log.info("[firma informativa] data.id={} sin x-signature/x-request-id: no se puede verificar", dataId);
            return;
        }
        ComponentesFirma componentes = ComponentesFirma.parse(xSignature);
        if (componentes == null) {
            log.warn("[firma informativa] data.id={} con x-signature mal formada: {}", dataId, xSignature);
            return;
        }
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("[firma informativa] data.id={}: app.secret-key no configurada", dataId);
            return;
        }
        String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, componentes.ts());
        if (FirmaWebhook.validar(secretKey, manifest, componentes.v1())) {
            log.info("[firma informativa] data.id={}: la firma HABRÍA validado", dataId);
        } else {
            log.warn("[firma informativa] data.id={}: la firma NO valida (ts={}, v1={}, huella secret={}); "
                            + "se procesa igual por modo permisivo",
                    dataId, componentes.ts(), prefijo(componentes.v1()), huella(secretKey));
        }
    }

    private static String prefijo(String valor) {
        if (valor == null || valor.length() <= 12) {
            return valor;
        }
        return valor.substring(0, 12) + "...";
    }

    /**
     * Huella no reversible del secret configurado, para poder compararlo con el que muestra Mercado Pago
     * sin exponerlo en los logs: {@code printf '%s' "<secret>" | sha256sum | cut -c1-12}.
     */
    private static String huella(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            return "no-disponible";
        }
    }

}
