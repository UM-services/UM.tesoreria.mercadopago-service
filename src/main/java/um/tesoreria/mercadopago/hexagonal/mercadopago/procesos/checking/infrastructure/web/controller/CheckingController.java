package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.service.CheckingService;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/checking")
@RequiredArgsConstructor
public class CheckingController {

    private final CheckingService checkingService;

    @GetMapping("/cuota/{chequeraCuotaId}")
    public ResponseEntity<String> checkingCuota(@PathVariable Long chequeraCuotaId) {
        return ResponseEntity.ok(checkingService.checkingCuota(chequeraCuotaId));
    }

    @GetMapping("/all/active")
    public ResponseEntity<String> checkingAllActive() {
        return ResponseEntity.ok(checkingService.checkingAllActive());
    }

}
