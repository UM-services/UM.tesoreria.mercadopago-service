package um.tesoreria.mercadopago.service.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.mercadopago.service.domain.dto.ChequeraCuotaDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraCuotaClient", path = "/api/tesoreria/core/chequeraCuota")
public interface ChequeraCuotaClient {

    @GetMapping("/chequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<ChequeraCuotaDto> findAllByChequera(@PathVariable Integer facultadId,
                                             @PathVariable Integer tipoChequeraId,
                                             @PathVariable Long chequeraSerieId,
                                             @PathVariable Integer alternativaId);

    @GetMapping("/chequera/pendientes/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<ChequeraCuotaDto> findAllPendientes(@PathVariable Integer facultadId,
                                         @PathVariable Integer tipoChequeraId,
                                         @PathVariable Long chequeraSerieId,
                                         @PathVariable Integer alternativaId);

    @GetMapping("/{chequeraCuotaId}")
    ChequeraCuotaDto findByChequeraCuotaId(@PathVariable Long chequeraCuotaId);

    @GetMapping("/unique/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{productoId}/{alternativaId}/{cuotaId}")
    ChequeraCuotaDto findByUnique(@PathVariable Integer facultadId,
                              @PathVariable Integer tipoChequeraId,
                              @PathVariable Long chequeraSerieId,
                              @PathVariable Integer productoId,
                              @PathVariable Integer alternativaId,
                              @PathVariable Integer cuotaId);

}
