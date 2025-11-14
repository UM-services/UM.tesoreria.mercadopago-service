package um.tesoreria.mercadopago.service.client.sender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tesoreria-sender-service", contextId = "chequeraClient", path = "/api/tesoreria/sender/chequera")
public interface ChequeraClient {

    @GetMapping("/sendCuota/{chequeraCuotaId}")
    String sendCuota(@PathVariable Long chequeraCuotaId);

}


