package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.TipoChequeraMercadoPagoCreditCardDto;

@FeignClient(name = "tesoreria-core-service", contextId = "tipoChequeraMercadoPagoCreditCardClient", path = "/api/tesoreria/core/tipoChequeraMercadoPagoCreditCard")
public interface TipoChequeraMercadoPagoCreditCardClient {

    @GetMapping("/unique/{tipoChequeraId}/{alternativaId}")
    TipoChequeraMercadoPagoCreditCardDto findByUnique(@PathVariable Integer tipoChequeraId,
                                                      @PathVariable Integer alternativaId);

}
