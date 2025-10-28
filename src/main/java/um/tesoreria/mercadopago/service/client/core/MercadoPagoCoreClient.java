package um.tesoreria.mercadopago.service.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.service.domain.dto.UMPreferenceMPDto;

@FeignClient(name = "tesoreria-core-service", contextId = "mercadoPagoCoreClient", path = "/api/tesoreria/core/mercadopago")
public interface MercadoPagoCoreClient {

    @GetMapping("/makeContext/{chequeraCuotaId}")
    UMPreferenceMPDto makeContext(@PathVariable Long chequeraCuotaId);

    @PutMapping("/updateContext/{mercadoPagoContextId}")
    MercadoPagoContextDto updateContext(@RequestBody MercadoPagoContextDto mercadoPagoContext, @PathVariable Long mercadoPagoContextId);

    @GetMapping("/find/context/{mercadoPagoContextId}")
    MercadoPagoContextDto findContextByMercadoPagoContextId(@PathVariable Long mercadoPagoContextId);

}
