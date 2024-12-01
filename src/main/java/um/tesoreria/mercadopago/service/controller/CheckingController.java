package um.tesoreria.mercadopago.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.service.service.CheckingService;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/checking")
public class CheckingController {

    private final CheckingService service;

    public CheckingController(CheckingService service) {
        this.service = service;
    }

    @GetMapping("/cuota/{chequeraCuotaId}")
    public ResponseEntity<String> checkingCuota(@PathVariable Long chequeraCuotaId) {
        return ResponseEntity.ok(service.checkingCuota(chequeraCuotaId));
    }

    @GetMapping("/extract/preference/{mercadoPagoContextId}")
    public ResponseEntity<Boolean> extractPreference(@PathVariable Long mercadoPagoContextId) {
        return ResponseEntity.ok(service.extractPreference(mercadoPagoContextId));
    }

    @GetMapping("/extract/preference/all")
    public ResponseEntity<Boolean> extractPreferenceAll() {
        return ResponseEntity.ok(service.extractPreferenceAll());
    }

}
