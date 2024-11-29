package um.tesoreria.mercadopago.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.service.client.core.ChequeraCuotaClient;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoContextClient;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.service.domain.dto.UMPreferenceMPDto;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChequeraService {

    private final ChequeraCuotaClient chequeraCuotaClient;
    private final PreferenceService preferenceService;
    private final MercadoPagoContextClient mercadoPagoContextClient;

    public ChequeraService(ChequeraCuotaClient chequeraCuotaClient, PreferenceService preferenceService, MercadoPagoContextClient mercadoPagoContextClient) {
        this.chequeraCuotaClient = chequeraCuotaClient;
        this.preferenceService = preferenceService;
        this.mercadoPagoContextClient = mercadoPagoContextClient;
    }

    public List<UMPreferenceMPDto> createChequeraContext(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId) {
        List<UMPreferenceMPDto> preferences = new ArrayList<>();
        for (var chequeraCuota : chequeraCuotaClient.findAllPendientes(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)) {
            preferenceService.createPreference(chequeraCuota.getChequeraCuotaId());
            MercadoPagoContextDto mercadoPagoContext = null;
            try {
                mercadoPagoContext = mercadoPagoContextClient.findActivoByChequeraCuotaId(chequeraCuota.getChequeraCuotaId());
            } catch (Exception e) {
                log.debug("MercadoPagoContext Error -> {}", e.getMessage());
            }
            preferences.add(UMPreferenceMPDto.builder()
                    .mercadoPagoContext(mercadoPagoContext)
                    .chequeraCuota(chequeraCuota)
                    .build());
        }
        return preferences;
    }

}
