package um.tesoreria.mercadopago.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.mercadopago.service.service.PreferenceService;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/preference")
public class PreferenceController {

    private final PreferenceService service;

    public PreferenceController(PreferenceService service) {
        this.service = service;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }

    @GetMapping("/create/{chequeraCuotaId}")
    public ResponseEntity<String> createPreference(@PathVariable Long chequeraCuotaId) {
        return ResponseEntity.ok(service.createPreference(chequeraCuotaId));
    }

}
