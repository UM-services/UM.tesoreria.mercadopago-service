package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
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
        var configurationUrl = environment.getProperty("app.notification-url");
        MercadoPagoConfig.setAccessToken(accessToken);

        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id("00002")
                .title("Cuota")
                .quantity(1)
                .unitPrice(new BigDecimal(100))
                .categoryId("others")
                .currencyId("ARS")
                .description("Arancel")
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

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name("Daniel")
                .surname("Quinteros")
                .email("daniel.quinterospinto@gmail.com")
                .build();

        String externalReference = "0100100001011002";
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("https://www.um.edu.ar")
                .pending("https://www.um.edu.ar")
                .failure("https://www.um.edu.ar")
                .build();
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(itemRequests)
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(externalReference)
                .notificationUrl(configurationUrl)
                .expires(false)
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

    public String processPayment(String xSignature, Payment payment) {
        var signature = environment.getProperty("app.x-signature");
        log.debug("xSignature -> {}", xSignature);
        if (!signature.equals(xSignature)) {
            throw new IllegalArgumentException("Invalid signature");
        }
        try {
            log.debug("Payment -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(payment));
        } catch (JsonProcessingException e) {
            log.debug("Payment Error -> {}", e.getMessage());
        }

        return "Payment processed";
    }

}
