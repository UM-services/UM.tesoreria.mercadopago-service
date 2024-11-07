package um.tesoreria.mercadopago.service.controller;

import com.mercadopago.resources.payment.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.mercadopago.service.domain.dto.PaymentDto;
import um.tesoreria.mercadopago.service.service.PreferenceService;

@RestController
@RequestMapping("/api/mercadopago/preference")
public class PreferenceController {

    private final PreferenceService service;

    public PreferenceController(PreferenceService service) {
        this.service = service;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }

    @GetMapping("/create_preference")
    public ResponseEntity<String> createPreference() {
        return ResponseEntity.ok(service.createPreference());
    }

    @PostMapping("/payment")
    public ResponseEntity<String> payment(@RequestHeader("x-signature") String xSignature, @RequestBody Payment payment) {
        return ResponseEntity.ok(service.processPayment(xSignature, payment));
    }

}
