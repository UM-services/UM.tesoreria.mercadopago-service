package um.tesoreria.mercadopago.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.service.domain.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.service.service.ChequeraService;

import java.util.List;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/chequera")
@RequiredArgsConstructor
public class ChequeraController {

    private final ChequeraService service;

    @GetMapping("/create/context/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    public ResponseEntity<List<UMPreferenceMPDto>> createChequeraContext(@PathVariable Integer facultadId, @PathVariable Integer tipoChequeraId, @PathVariable Long chequeraSerieId, @PathVariable Integer alternativaId) {
        return ResponseEntity.ok(service.createChequeraContext(facultadId, tipoChequeraId, chequeraSerieId, alternativaId));
    }

}
