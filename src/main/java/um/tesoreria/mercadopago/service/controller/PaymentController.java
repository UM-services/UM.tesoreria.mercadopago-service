package um.tesoreria.mercadopago.service.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.service.service.PaymentService;

@RestController
@RequestMapping("/api/mercadopago/payment")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/listener")
    public ResponseEntity<String> listener(HttpServletRequest request, @RequestParam("data.id") String dataId) {
        return ResponseEntity.ok(service.processPaymentWebhook(request, dataId));
    }

}
