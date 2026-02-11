package um.tesoreria.mercadopago.service.controller;

import com.mercadopago.resources.payment.Payment;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.service.service.PaymentService;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/listener")
    public ResponseEntity<String> listener(HttpServletRequest request,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestBody(required = false) um.tesoreria.mercadopago.service.domain.dto.PaymentNotificationDto notification) {
        if (dataId == null && notification != null && notification.getData() != null) {
            dataId = notification.getData().getId();
        }
        return ResponseEntity.ok(service.processPaymentWebhook(request, dataId));
    }

    @GetMapping("/update/{dataId}")
    public ResponseEntity<Void> update(@PathVariable String dataId) {
        service.retrieveAndPublishPayment(dataId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/faltantes")
    public ResponseEntity<Void> fillFaltantes() {
        service.fillFaltantes();
        return ResponseEntity.ok().build();
    }

}
