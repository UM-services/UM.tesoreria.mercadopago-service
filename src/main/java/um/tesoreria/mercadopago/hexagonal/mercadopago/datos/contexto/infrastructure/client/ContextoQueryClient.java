package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "mercadoPagoContextClient", path = "/api/tesoreria/core/mercadoPagoContext")
public interface ContextoQueryClient {

    @GetMapping("/all/active/to/change")
    List<Long> findAllActiveToChange();

}
