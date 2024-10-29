package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.mercadopago.MercadoPagoConfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PreferenceService {

    private final Environment environment;

    public PreferenceService(Environment environment) {
        this.environment = environment;
    }

    public String createPreference() {
        var accessToken = environment.getProperty("app.access-token");
        log.debug("Access Token -> {}", accessToken);
        MercadoPagoConfig.setAccessToken(accessToken);

        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .title("Cuota")
                .quantity(1)
                .unitPrice(new BigDecimal(100))
                .build();
        try {
            log.debug("ItemRequest -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(itemRequest));
        } catch (JsonProcessingException e) {
            log.debug("Item Request Error -> {}", e.getMessage());
        }
        List<PreferenceItemRequest> itemRequests = new ArrayList<>();
        itemRequests.add(itemRequest);
        try {
            log.debug("ItemRequests -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(itemRequests));
        } catch (JsonProcessingException e) {
            log.debug("Item Requests Error -> {}", e.getMessage());
        }

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(itemRequests)
                .build();
        try {
            log.debug("PreferenceRequest -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(preferenceRequest));
        } catch (JsonProcessingException e) {
            log.debug("Preference Request Error -> {}", e.getMessage());
        }

        PreferenceClient preferenceClient = new PreferenceClient();
        try {
            Preference preference = preferenceClient.create(preferenceRequest);
            try {
                log.debug("Preference -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(preference));
            } catch (JsonProcessingException e) {
                log.debug("Preference Error -> {}", e.getMessage());
            }
        } catch (MPException | MPApiException e) {
            log.debug("MercadoPago Error -> {}", e.getMessage());
        }
        return "Preference created";
    }

}
