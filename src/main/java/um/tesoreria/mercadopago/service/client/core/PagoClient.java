package um.tesoreria.mercadopago.service.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.mercadopago.service.domain.dto.ChequeraPagoDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/pago")
public interface PagoClient {

    @GetMapping("/registra/pago/mercadopago/{mercadoPagoContextId}")
    ChequeraPagoDto registrarPagoMercadoPago(@PathVariable Long mercadoPagoContextId);

}
