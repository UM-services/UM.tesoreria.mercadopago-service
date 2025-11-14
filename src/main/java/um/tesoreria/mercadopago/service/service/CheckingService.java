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

    public String checkingCuota(Long chequeraCuotaId) {
        log.debug("Processing CheckingService.checkingCuota -> {}", chequeraCuotaId);
        preferenceService.createPreference(chequeraCuotaId);

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
