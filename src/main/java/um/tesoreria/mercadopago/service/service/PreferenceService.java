package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.mercadopago.MercadoPagoConfig;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoCoreClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
    private final MercadoPagoCoreClient mercadoPagoCoreClient;

    public PreferenceService(Environment environment,
                             MercadoPagoCoreClient mercadoPagoCoreClient) {
        this.environment = environment;
        this.mercadoPagoCoreClient = mercadoPagoCoreClient;
    }

    public String createPreference(Long chequeraCuotaId) {

        var umPreferenceMPDto = mercadoPagoCoreClient.makeContext(chequeraCuotaId);
        if (umPreferenceMPDto == null)  {
            log.error("No se encontro el MercadoPagoContext con el chequeraCuotaId: {}", chequeraCuotaId);
            return "No se encontro el MercadoPagoContext con el chequeraCuotaId: " + chequeraCuotaId;
        }

        try {
            log.debug("UMPreferenceMPDto -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(umPreferenceMPDto));
        } catch (JsonProcessingException e) {
            log.debug("UMPreferenceMPDto Error -> {}", e.getMessage());
        }

        var accessToken = environment.getProperty("app.access-token");
        log.debug("Access Token -> {}", accessToken);
        var configurationUrl = environment.getProperty("app.notification-url");
        log.debug("Configuration URL -> {}", configurationUrl);

        MercadoPagoConfig.setAccessToken(accessToken);

        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id("item-key-" + umPreferenceMPDto.getChequeraCuota().getFacultadId() + "-" + umPreferenceMPDto.getChequeraCuota().getTipoChequeraId() + "-" + umPreferenceMPDto.getChequeraCuota().getChequeraSerieId() + "-" + umPreferenceMPDto.getChequeraCuota().getProductoId() + "-" + umPreferenceMPDto.getChequeraCuota().getAlternativaId() + "-" + umPreferenceMPDto.getChequeraCuota().getCuotaId() + "-id-" + umPreferenceMPDto.getChequeraCuota().getChequeraCuotaId())
                .title(umPreferenceMPDto.getChequeraCuota().getProducto().getNombre())
                .quantity(1)
                .unitPrice(umPreferenceMPDto.getMercadoPagoContext().getImporte())
                .categoryId("others")
                .currencyId("ARS")
                .description(umPreferenceMPDto.getChequeraCuota().getProducto().getNombre() + "-Periodo-" + umPreferenceMPDto.getChequeraCuota().getMes() + "-" + umPreferenceMPDto.getChequeraCuota().getAnho())
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
                .name(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getPersona().getNombre())
                .surname(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getPersona().getApellido())
                .email(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getDomicilio().getEmailPersonal())
                .build();

        String externalReference = String.format("%02d", umPreferenceMPDto.getChequeraCuota().getFacultadId())
                + String.format("%03d", umPreferenceMPDto.getChequeraCuota().getTipoChequeraId())
                + String.format("%05d", umPreferenceMPDto.getChequeraCuota().getChequeraSerieId())
                + String.format("%02d", umPreferenceMPDto.getChequeraCuota().getProductoId())
                + String.format("%02d", umPreferenceMPDto.getChequeraCuota().getAlternativaId())
                + String.format("%04d", umPreferenceMPDto.getChequeraCuota().getCuotaId())
                + "-" + umPreferenceMPDto.getChequeraCuota().getChequeraCuotaId()
                + "-" + umPreferenceMPDto.getMercadoPagoContext().getMercadoPagoContextId();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("https://www.um.edu.ar")
                .pending("https://www.um.edu.ar")
                .failure("https://www.um.edu.ar")
                .build();

        // Excluimos tarjetas de crédito
        PreferencePaymentTypeRequest creditCard = PreferencePaymentTypeRequest.builder()
                .id("credit_card")
                .build();
        List<PreferencePaymentTypeRequest> excludedPaymentTypes = new ArrayList<>();
        excludedPaymentTypes.add(creditCard);
        PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                .excludedPaymentTypes(excludedPaymentTypes)
                .build();
        // Creamos el preference
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(itemRequests)
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(externalReference)
                .notificationUrl(configurationUrl)
                .expires(true)
                .expirationDateTo(umPreferenceMPDto.getMercadoPagoContext().getFechaVencimiento())
                .paymentMethods(paymentMethods)
                .build();
        try {
            log.debug("PreferenceRequest -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(preferenceRequest));
        } catch (JsonProcessingException e) {
            log.debug("Preference Request Error -> {}", e.getMessage());
        }

        PreferenceClient preferenceClient = new PreferenceClient();
        Preference preference = null;
        var mercadoPagoContext = umPreferenceMPDto.getMercadoPagoContext();

        try {
            preference = preferenceClient.create(preferenceRequest);
            mercadoPagoContext.setInitPoint(preference.getInitPoint());
            try {
                var preferenceString = JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(preference);
                mercadoPagoContext.setPreference(preferenceString);
                log.debug("Preference -> {}", preferenceString);
            } catch (JsonProcessingException e) {
                log.debug("Preference Error -> {}", e.getMessage());
            }

            mercadoPagoContext = mercadoPagoCoreClient.updateContext(mercadoPagoContext.getMercadoPagoContextId(), mercadoPagoContext);

        } catch (MPException | MPApiException e) {
            log.debug("MercadoPago Error -> {}", e.getMessage());
        }

        String preferenceString = "";
        String mercadoPagoContextString = "";
        try {
            preferenceString = JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(preference);
            mercadoPagoContextString = JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(mercadoPagoContext);
        } catch (JsonProcessingException e) {
            log.debug("Preference Error -> {}", e.getMessage());
        }

        return "Preference created -> " + preferenceString + " MercadoPagoContext -> " + mercadoPagoContextString;
    }

}
