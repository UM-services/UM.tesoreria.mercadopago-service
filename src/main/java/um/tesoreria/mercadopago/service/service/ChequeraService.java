package um.tesoreria.mercadopago.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.service.client.core.ChequeraCuotaClient;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoContextClient;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.service.domain.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.service.util.Jsonifier;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChequeraService {

    private final ChequeraCuotaClient chequeraCuotaClient;
    private final PreferenceService preferenceService;
    private final MercadoPagoContextClient mercadoPagoContextClient;

    public List<UMPreferenceMPDto> createChequeraContext(Integer facultadId,
                                                         Integer tipoChequeraId,
                                                         Long chequeraSerieId,
                                                         Integer alternativaId) {
        log.debug("Processing ChequeraService.createChequeraContext");
        List<UMPreferenceMPDto> preferences = new ArrayList<>();
        for (var chequeraCuota : chequeraCuotaClient.findAllPendientes(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)) {
            preferenceService.createPreference(chequeraCuota.getChequeraCuotaId());
            MercadoPagoContextDto mercadoPagoContext = null;
            try {
                mercadoPagoContext = mercadoPagoContextClient.findActivoByChequeraCuotaId(chequeraCuota.getChequeraCuotaId());
            } catch (Exception e) {
                log.error("MercadoPagoContext Error -> {}", e.getMessage());
            }
            assert mercadoPagoContext != null;
            preferences.add(UMPreferenceMPDto.builder()
                    .mercadoPagoContext(mercadoPagoContext)
                    .chequeraCuota(chequeraCuota)
                    .build());
        }
        return preferences;
    }

}
