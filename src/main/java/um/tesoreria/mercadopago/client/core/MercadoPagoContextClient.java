package um.tesoreria.mercadopago.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.mercadopago.domain.dto.MercadoPagoContextDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "mercadoPagoContextClient", path = "/api/tesoreria/core/mercadoPagoContext")
public interface MercadoPagoContextClient {

    @GetMapping("/cuota/activo/{chequeraCuotaId}")
    MercadoPagoContextDto findActivoByChequeraCuotaId(@PathVariable Long chequeraCuotaId);

    @GetMapping("/{mercadoPagoContextId}")
    MercadoPagoContextDto findByMercadoPagoContextId(@PathVariable Long mercadoPagoContextId);

    @GetMapping("/all/active/cuota/id")
    List<Long> findAllActiveChequeraCuota();

    @GetMapping("/all/active/to/change")
    List<Long> findAllActiveToChange();

    @GetMapping("/pagos/sin/imputar")
    List<MercadoPagoContextDto> findAllSinImputar();

}
