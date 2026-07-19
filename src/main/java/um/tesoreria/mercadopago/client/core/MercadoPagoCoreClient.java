package um.tesoreria.mercadopago.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.mercadopago.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.domain.dto.UMPreferenceMPDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "mercadoPagoCoreClient", path = "/api/tesoreria/core/mercadopago")
public interface MercadoPagoCoreClient {

    @GetMapping("/makeContext/{chequeraCuotaId}")
    UMPreferenceMPDto makeContext(@PathVariable Long chequeraCuotaId);

    @GetMapping("/makeContextsChequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<UMPreferenceMPDto> makeContextsChequera(
        @PathVariable Integer facultadId,
        @PathVariable Integer tipoChequeraId,
        @PathVariable Long chequeraSerieId,
        @PathVariable Integer alternativaId
    );

    @PutMapping("/updateContext/{mercadoPagoContextId}")
    MercadoPagoContextDto updateContext(@RequestBody MercadoPagoContextDto mercadoPagoContext, @PathVariable Long mercadoPagoContextId);

    @PutMapping("/updateContexts")
    List<MercadoPagoContextDto> updateContexts(@RequestBody List<MercadoPagoContextDto> contexts);

    @GetMapping("/find/context/{mercadoPagoContextId}")
    MercadoPagoContextDto findContextByMercadoPagoContextId(@PathVariable Long mercadoPagoContextId);

}
