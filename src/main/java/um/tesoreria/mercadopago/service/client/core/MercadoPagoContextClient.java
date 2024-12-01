package um.tesoreria.mercadopago.service.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/mercadoPagoContext")
public interface MercadoPagoContextClient {

    @GetMapping("/cuota/activo/{chequeraCuotaId}")
    MercadoPagoContextDto findActivoByChequeraCuotaId(@PathVariable Long chequeraCuotaId);

    @GetMapping("/{mercadoPagoContextId}")
    MercadoPagoContextDto findByMercadoPagoContextId(@PathVariable Long mercadoPagoContextId);

    @GetMapping("/all/id")
    List<Long> findAllIds();

}
