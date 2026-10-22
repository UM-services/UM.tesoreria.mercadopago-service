package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.PreferenciaPatchDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.RenovacionesCandidatosRequestDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;

import java.time.OffsetDateTime;
import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "mercadoPagoCoreClient", path = "/api/tesoreria/core/mercadopago")
public interface ContextoCoreClient {

    @GetMapping("/makeContext/{chequeraCuotaId}")
    UMPreferenceMPDto makeContext(@PathVariable Long chequeraCuotaId);

    @GetMapping("/makeContextsChequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<UMPreferenceMPDto> makeContextsChequera(@PathVariable Integer facultadId,
                                                 @PathVariable Integer tipoChequeraId,
                                                 @PathVariable Long chequeraSerieId,
                                                 @PathVariable Integer alternativaId);

    @PutMapping("/updateContext/{mercadoPagoContextId}")
    MercadoPagoContextDto updateContext(@RequestBody MercadoPagoContextDto mercadoPagoContext,
                                        @PathVariable Long mercadoPagoContextId);

    @PutMapping("/updateContexts")
    List<MercadoPagoContextDto> updateContexts(@RequestBody List<MercadoPagoContextDto> contexts);

    @PostMapping("/renovaciones")
    List<UMPreferenceMPDto> findRenovaciones(@RequestBody RenovacionesCandidatosRequestDto request);

    @PatchMapping("/updateContext/{mercadoPagoContextId}/preferencia")
    void updatePreferencia(@RequestBody PreferenciaPatchDto patch,
                           @PathVariable Long mercadoPagoContextId);

}
