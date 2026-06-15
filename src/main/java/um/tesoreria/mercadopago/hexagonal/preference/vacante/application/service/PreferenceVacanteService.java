package um.tesoreria.mercadopago.hexagonal.preference.vacante.application.service;

import com.mercadopago.client.preference.PreferenceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.domain.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.preference.service.PreferenceService;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreferenceVacanteService {

    private final PreferenceService preferenceService;

    public MercadoPagoContextDto createPreference(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("\n\nProcessing PreferenceCuotaService.createPreference\n\n");
        if (umPreferenceMPDto == null) {
            throw new IllegalArgumentException("UMPreferenceMPDto cannot be null");
        }
        log.debug("UMPreferenceMP -> {}", umPreferenceMPDto.jsonify());
        var mercadoPagoContext = umPreferenceMPDto.getMercadoPagoContext();
        if (mercadoPagoContext != null && mercadoPagoContext.getInitPoint() != null) {
//            if (mercadoPagoContext.getChanged() == 1) {
//                mercadoPagoContext = updatePreference(mercadoPagoContext);
//            }
            return mercadoPagoContext;
        }
        var reservaVacante = umPreferenceMPDto.getReservaVacante();
        preferenceService.setAccessTokenAndLog();
        PreferenceRequest preferenceRequest = preferenceService.buildPreferenceRequest(umPreferenceMPDto, null);
        assert mercadoPagoContext != null;
        return preferenceService.createAndLogPreference(preferenceRequest, mercadoPagoContext);
    }

}
