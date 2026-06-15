package um.tesoreria.mercadopago.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.client.core.MercadoPagoContextClient;
import um.tesoreria.mercadopago.hexagonal.preference.cuota.application.service.PreferenceCuotaService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckingService {

    private final PreferenceCuotaService preferenceCuotaService;
    private final MercadoPagoContextClient mercadoPagoContextClient;
    private final MercadoPagoCoreClient mercadoPagoCoreClient;

    public String checkingCuota(Long chequeraCuotaId) {
        log.debug("Processing CheckingService.checkingCuota -> {}", chequeraCuotaId);
        var umPreferenceMPDto = mercadoPagoCoreClient.makeContext(chequeraCuotaId);
        if (umPreferenceMPDto != null) {
            var context = preferenceCuotaService.createPreference(umPreferenceMPDto);
            if (context != null) {
                try {
                    mercadoPagoCoreClient.updateContext(context, context.getMercadoPagoContextId());
                } catch (Exception e) {
                    log.error("Error updating context in Core: {}", e.getMessage());
                }
            }
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
