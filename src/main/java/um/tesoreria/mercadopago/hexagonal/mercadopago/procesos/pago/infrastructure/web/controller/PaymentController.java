package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.PaymentNotificationDto;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PagoService pagoService;

    @PostMapping("/listener")
    public ResponseEntity<String> listener(HttpServletRequest request,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestBody(required = false) PaymentNotificationDto notification) {
        if (dataId == null && notification != null && notification.getData() != null) {
            dataId = notification.getData().getId();
        }
        var cabeceras = new CabecerasWebhook(request.getHeader("x-signature"), request.getHeader("x-request-id"));
        return ResponseEntity.ok(pagoService.processPaymentWebhook(cabeceras, dataId));
    }

    @GetMapping("/update/{dataId}")
    public ResponseEntity<Void> update(@PathVariable String dataId) {
        pagoService.retrieveAndPublishPayment(dataId);
        return ResponseEntity.ok().build();
    }

}
