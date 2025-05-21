package um.tesoreria.mercadopago.service.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.mercadopago.service.domain.dto.TipoChequeraMercadoPagoCreditCardDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/tipoChequeraMercadoPagoCreditCard")
public interface TipoChequeraMercadoPagoCreditCardClient {

    @GetMapping("/unique/{tipoChequeraId}/{alternativaId}")
    TipoChequeraMercadoPagoCreditCardDto findByUnique(@PathVariable Integer tipoChequeraId, @PathVariable Integer alternativaId);

}
