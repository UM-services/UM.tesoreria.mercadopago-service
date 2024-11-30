package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.service.client.core.ChequeraCuotaClient;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoContextClient;
import um.tesoreria.mercadopago.service.domain.dto.ChequeraCuotaDto;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;

@Service
@Slf4j
public class CheckingService {

    private final Environment environment;
    private final ChequeraCuotaClient chequeraCuotaClient;
    private final MercadoPagoContextClient mercadoPagoContextClient;

    public CheckingService(ChequeraCuotaClient chequeraCuotaClient,
                           MercadoPagoContextClient mercadoPagoContextClient,
                           Environment environment) {
        this.chequeraCuotaClient = chequeraCuotaClient;
        this.mercadoPagoContextClient = mercadoPagoContextClient;
        this.environment = environment;
    }

    public String checkingCuota(Long chequeraCuotaId) {
        var chequeraCuota = chequeraCuotaClient.findByChequeraCuotaId(chequeraCuotaId);
        logChequeraCuota(chequeraCuota);
        var mercadoPagoContext = mercadoPagoContextClient.findActivoByChequeraCuotaId(chequeraCuotaId);
        logMercadoPagoContext(mercadoPagoContext);

        var accessToken = environment.getProperty("app.access-token");

        MercadoPagoConfig.setAccessToken(accessToken);

        PreferenceClient preferenceClient = new PreferenceClient();
        Preference preference = null;

        try {
            preference = preferenceClient.get(mercadoPagoContext.getPreferenceId());
            logPreference(preference);
        } catch (MPException | MPApiException e) {
            throw new RuntimeException(e);
        }

        return "Checking";
    }

    private void logPreference(Preference preference) {
        try {
            log.debug("Preference -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(preference));
        } catch (JsonProcessingException e) {
            log.debug("Preference Error -> {}", e.getMessage());
        }
    }

    private void logMercadoPagoContext(MercadoPagoContextDto mercadoPagoContext) {
        try {
            log.debug("MercadoPagoContext -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(mercadoPagoContext));
        } catch (JsonProcessingException e) {
            log.debug("MercadoPagoContext Error -> {}", e.getMessage());
        }
    }

    private void logChequeraCuota(ChequeraCuotaDto chequeraCuota) {
        try {
            log.debug("ChequeraCuota -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(chequeraCuota));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraCuota Error -> {}", e.getMessage());
        }
    }

}
