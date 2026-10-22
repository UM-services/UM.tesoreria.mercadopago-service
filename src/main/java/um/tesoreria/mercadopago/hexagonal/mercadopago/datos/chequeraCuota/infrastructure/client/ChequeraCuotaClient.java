package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ChequeraCuotaDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraCuotaClient", path = "/api/tesoreria/core/chequeraCuota")
public interface ChequeraCuotaClient {

    @GetMapping("/chequera/pendientes/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<ChequeraCuotaDto> findAllPendientes(@PathVariable Integer facultadId,
                                             @PathVariable Integer tipoChequeraId,
                                             @PathVariable Long chequeraSerieId,
                                             @PathVariable Integer alternativaId);

    @GetMapping("/{chequeraCuotaId}")
    ChequeraCuotaDto findByChequeraCuotaId(@PathVariable Long chequeraCuotaId);

}
