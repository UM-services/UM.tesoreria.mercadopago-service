package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import um.tesoreria.mercadopago.service.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.service.domain.dto.ChequeraCuotaDto;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.service.util.DateToolMP;

@Service
@Slf4j
public class CheckingService {

    private final Environment environment;
    private final ChequeraCuotaClient chequeraCuotaClient;
    private final MercadoPagoContextClient mercadoPagoContextClient;
    private final PreferenceService preferenceService;
    private final MercadoPagoCoreClient mercadoPagoCoreClient;

    public CheckingService(ChequeraCuotaClient chequeraCuotaClient,
                           MercadoPagoContextClient mercadoPagoContextClient,
                           Environment environment, PreferenceService preferenceService, MercadoPagoCoreClient mercadoPagoCoreClient) {
        this.chequeraCuotaClient = chequeraCuotaClient;
        this.mercadoPagoContextClient = mercadoPagoContextClient;
        this.environment = environment;
        this.preferenceService = preferenceService;
        this.mercadoPagoCoreClient = mercadoPagoCoreClient;
    }

    public String checkingCuota(Long chequeraCuotaId) {
        log.debug("Processing checkingCuota");
        var chequeraCuota = chequeraCuotaClient.findByChequeraCuotaId(chequeraCuotaId);
        logChequeraCuota(chequeraCuota);

        var accessToken = environment.getProperty("app.access-token");
        MercadoPagoConfig.setAccessToken(accessToken);

        preferenceService.createPreference(chequeraCuotaId);

        PreferenceClient preferenceClient = new PreferenceClient();
        try {
            var mercadoPagoContext = mercadoPagoContextClient.findActivoByChequeraCuotaId(chequeraCuotaId);
            logMercadoPagoContext(mercadoPagoContext);
            var preference = preferenceClient.get(mercadoPagoContext.getPreferenceId());
            logPreference(preference);
            log.debug("Comparing Dates");
            if (!DateToolMP.convertToMPDate(mercadoPagoContext.getFechaVencimiento()).isEqual(preference.getExpirationDateTo())) {
                log.debug("Updating vencimiento");
                preferenceService.updatePreference(mercadoPagoContext);
            }
        } catch (MPException | MPApiException e) {
            log.debug("Preference Error -> {}", e.getMessage());
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

    public Boolean extractPreference(Long mercadoPagoContextId) {
        var mercadoPagoContext = mercadoPagoContextClient.findByMercadoPagoContextId(mercadoPagoContextId);
        if (mercadoPagoContext.getPreferenceId() == null) {
            String jsonString = mercadoPagoContext.getPreference(); // Aquí coloca tu JSON completo

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonString);

                // Extraer el valor de "id"
                String id = rootNode.path("id").asText();
                log.debug("preference id: {}", id);
                mercadoPagoContext.setPreferenceId(id);
                mercadoPagoCoreClient.updateContext(mercadoPagoContext, mercadoPagoContextId);
                return true;
            } catch (Exception e) {
                log.debug("Error extracting id: {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    public Boolean extractPreferenceAll() {
        var ids = mercadoPagoContextClient.findAllIds();
        return ids.stream().allMatch(this::extractPreference);
    }

}
