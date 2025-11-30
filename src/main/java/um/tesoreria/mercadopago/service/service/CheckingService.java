package um.tesoreria.mercadopago.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoContextClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckingService {

    private final PreferenceService preferenceService;
    private final MercadoPagoContextClient mercadoPagoContextClient;
    private final um.tesoreria.mercadopago.service.client.core.MercadoPagoCoreClient mercadoPagoCoreClient;

    public String checkingCuota(Long chequeraCuotaId) {
        log.debug("Processing CheckingService.checkingCuota -> {}", chequeraCuotaId);
        var umPreferenceMPDto = mercadoPagoCoreClient.makeContext(chequeraCuotaId);
        if (umPreferenceMPDto != null) {
            preferenceService.createPreference(umPreferenceMPDto);
        }

        return "Checked";
    }

    public String checkingAllActive() {
        log.debug("Processing CheckingService.checkingAllActive");
        for (var chequeraCuotaId : mercadoPagoContextClient.findAllActiveToChange()) {
            log.debug(checkingCuota(chequeraCuotaId));
        }
        return "Checked";
    }

}
