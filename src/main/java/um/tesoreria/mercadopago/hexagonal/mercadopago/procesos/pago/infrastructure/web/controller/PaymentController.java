package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.service.PagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ResultadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.PaymentNotificationDto;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private static final String TOPIC_PAGO = "payment";

    private final PagoService pagoService;

    @PostMapping("/listener")
    public ResponseEntity<String> listener(HttpServletRequest request,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestBody(required = false) PaymentNotificationDto notification) {
        var cabeceras = new CabecerasWebhook(request.getHeader("x-signature"), request.getHeader("x-request-id"));
        String dataIdResuelto = resolverDataId(dataId, notification);

        if (dataIdResuelto != null) {
            return responder(pagoService.processPaymentWebhook(cabeceras, dataIdResuelto));
        }

        if (esNotificacionLegadaDePago(id, topic)) {
            log.debug("Notificación IPN legacy de pago: id={}", id);
            return responder(pagoService.processLegacyPaymentNotification(id));
        }

        if (id != null && !id.isBlank()) {
            log.debug("Notificación IPN ignorada: topic={}, id={}", topic, id);
            return ResponseEntity.ok("Notificación ignorada: topic " + topic);
        }

        return responder(pagoService.processPaymentWebhook(cabeceras, null));
    }

    @GetMapping("/update/{dataId}")
    public ResponseEntity<Void> update(@PathVariable String dataId) {
        return pagoService.retrieveAndPublishPayment(dataId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    private String resolverDataId(String dataId, PaymentNotificationDto notification) {
        if (dataId != null && !dataId.isBlank()) {
            return dataId;
        }
        if (notification != null && notification.getData() != null
                && notification.getData().getId() != null && !notification.getData().getId().isBlank()) {
            return notification.getData().getId();
        }
        return null;
    }

    private boolean esNotificacionLegadaDePago(String id, String topic) {
        return id != null && !id.isBlank() && TOPIC_PAGO.equalsIgnoreCase(topic);
    }

    private ResponseEntity<String> responder(ResultadoWebhookPago resultado) {
        HttpStatus status = switch (resultado.estado()) {
            case PROCESADO, LEGACY_IGNORADA -> HttpStatus.OK;
            case DATA_ID_FALTANTE, DATA_ID_INVALIDO -> HttpStatus.BAD_REQUEST;
            case FIRMA_FALTANTE, REQUEST_ID_FALTANTE, FIRMA_MAL_FORMADA, FIRMA_INVALIDA -> HttpStatus.UNAUTHORIZED;
            case PAGO_NO_ENCONTRADO -> HttpStatus.BAD_GATEWAY;
        };
        return ResponseEntity.status(status).body(resultado.mensaje());
    }

}
