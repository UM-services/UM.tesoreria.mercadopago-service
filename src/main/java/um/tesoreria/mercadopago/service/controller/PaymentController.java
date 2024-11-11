package um.tesoreria.mercadopago.service.controller;

import com.mercadopago.resources.payment.Payment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/update/{dataId}")
    public ResponseEntity<Payment> update(@PathVariable String dataId) {
        return ResponseEntity.ok(service.retrieveAndSavePayment(dataId));
    }

}
