package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.application.service.ChequeraService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.PreferenciaDtoMapper;

import java.util.List;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/chequera")
@RequiredArgsConstructor
public class ChequeraController {

    private final ChequeraService chequeraService;
    private final PreferenciaDtoMapper preferenciaDtoMapper;

    @GetMapping("/create/context/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    public ResponseEntity<List<UMPreferenceMPDto>> createChequeraContext(@PathVariable Integer facultadId,
                                                                          @PathVariable Integer tipoChequeraId,
                                                                          @PathVariable Long chequeraSerieId,
                                                                          @PathVariable Integer alternativaId) {
        return ResponseEntity.ok(chequeraService.createChequeraContext(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)
                .stream()
                .map(preferenciaDtoMapper::toDto)
                .toList());
    }

    @GetMapping("/create/context/bulk/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    public ResponseEntity<List<UMPreferenceMPDto>> createChequeraContextBulk(@PathVariable Integer facultadId,
                                                                              @PathVariable Integer tipoChequeraId,
                                                                              @PathVariable Long chequeraSerieId,
                                                                              @PathVariable Integer alternativaId) {
        return ResponseEntity.ok(chequeraService.createChequeraContextBulk(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)
                .stream()
                .map(preferenciaDtoMapper::toDto)
                .toList());
    }

}
