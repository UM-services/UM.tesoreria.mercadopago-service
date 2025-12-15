package um.tesoreria.mercadopago.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.service.client.core.ChequeraCuotaClient;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.service.domain.dto.UMPreferenceMPDto;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChequeraService {

    private final ChequeraCuotaClient chequeraCuotaClient;
    private final PreferenceService preferenceService;
    private final MercadoPagoCoreClient mercadoPagoCoreClient;

    public List<UMPreferenceMPDto> createChequeraContext(Integer facultadId,
            Integer tipoChequeraId,
            Long chequeraSerieId,
            Integer alternativaId) {
        log.debug("\n\nProcessing ChequeraService.createChequeraContext\n\n");
        List<UMPreferenceMPDto> preferences = new ArrayList<>();
        for (var chequeraCuota : chequeraCuotaClient.findAllPendientes(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId)) {
            var umPreferenceMPDto = mercadoPagoCoreClient.makeContext(chequeraCuota.getChequeraCuotaId());
            if (umPreferenceMPDto != null) {
                var mercadoPagoContext = preferenceService.createPreference(umPreferenceMPDto);
                if (mercadoPagoContext != null) {
                    try {
                        mercadoPagoCoreClient.updateContext(mercadoPagoContext,
                                mercadoPagoContext.getMercadoPagoContextId());
                    } catch (Exception e) {
                        log.error("Error updating context in Core: {}", e.getMessage());
                    }
                    preferences.add(UMPreferenceMPDto.builder()
                            .mercadoPagoContext(mercadoPagoContext)
                            .chequeraCuota(chequeraCuota)
                            .build());
                }
            }
        }
        return preferences;
    }

}
