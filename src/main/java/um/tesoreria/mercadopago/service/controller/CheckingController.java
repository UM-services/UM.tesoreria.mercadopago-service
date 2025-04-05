package um.tesoreria.mercadopago.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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

    @GetMapping("/all/active")
    @Scheduled(cron = "0 0 3 * * *")
    public ResponseEntity<String> checkingAllActive() {
        return ResponseEntity.ok(service.checkingAllActive());
    }

    @GetMapping("/11/12/2024")
    public ResponseEntity<Void> checking_2024_11_12() {
        service.checking_2024_11_12();
        return ResponseEntity.ok().build();
    }

}
