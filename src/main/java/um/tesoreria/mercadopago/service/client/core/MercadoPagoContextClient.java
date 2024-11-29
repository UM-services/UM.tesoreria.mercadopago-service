package um.tesoreria.mercadopago.service.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/mercadoPagoContext")
public interface MercadoPagoContextClient {

    @GetMapping("/cuota/activo/{chequeraCuotaId}")
    MercadoPagoContextDto findActivoByChequeraCuotaId(@PathVariable Long chequeraCuotaId);

}
