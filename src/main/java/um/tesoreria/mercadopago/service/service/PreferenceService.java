package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.mercadopago.MercadoPagoConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
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
        log.debug("Configuration URL -> {}", configurationUrl);

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

    public String processPayment(HttpServletRequest request, Payment payment) {
        // Obtener el header x-signature y x-request-id
        String xSignature = request.getHeader("x-signature");
        String xRequestId = request.getHeader("x-request-id");

        log.debug("Signature from MP -> {}", xSignature);

        if (xSignature == null || xSignature.isEmpty()) {
            log.error("Falta el header x-signature");
            return "Falta el header x-signature";
        }

        // Extraer ts y v1 del x-signature
        String ts = null;
        String v1 = null;
        String[] parts = xSignature.split(",");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if ("ts".equals(key)) {
                    ts = value;
                } else if ("v1".equals(key)) {
                    v1 = value;
                }
            }
        }

        if (ts == null || v1 == null) {
            log.error("Formato inválido en el header x-signature");
            return "Formato inválido en el header x-signature";
        }

        // Obtener data.id del payment
        Long dataId = payment.getId();
        if (dataId == 0) {
            log.error("Falta el parámetro dataId");
            return "Falta el parámetro data.id";
        }

        if (xRequestId == null || xRequestId.isEmpty()) {
            log.error("Falta el header x-request-id");
            return "Falta el header x-request-id";
        }

        // Construir el template
        String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, ts);
        log.debug("Manifest string: {}", manifest);

        // Obtener la clave secreta
        String secret = environment.getProperty("app.secret-key");
        if (secret == null || secret.isEmpty()) {
            log.error("Clave secreta no configurada");
            return "Clave secreta no configurada";
        }

        // Generar el HMAC SHA256
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacData = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));

            // Convertir a cadena hexadecimal
            String generatedSignature = HexFormat.of().formatHex(hmacData);
            log.debug("Generated signature: {}", generatedSignature);

            if (generatedSignature.equalsIgnoreCase(v1)) {
                // Verificación exitosa
                log.debug("HMAC verification passed");
            } else {
                // Verificación fallida
                log.error("HMAC verification failed");
                return "Verificación fallida";
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error al generar la firma HMAC", e);
            return "Error al procesar el pago";
        }

        // Procesar el pago
        try {
            String paymentJson = JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payment);
            log.debug("Payment -> {}", paymentJson);
        } catch (JsonProcessingException e) {
            log.error("Payment Error -> {}", e.getMessage());
        }

        return "Payment processed";
    }
}
