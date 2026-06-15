package um.tesoreria.mercadopago.hexagonal.preference.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.domain.dto.TipoChequeraMercadoPagoCreditCardDto;
import um.tesoreria.mercadopago.domain.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.util.DateToolMP;
import um.tesoreria.mercadopago.util.Jsonifier;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreferenceService {

    private final Environment environment;
    private final MercadoPagoCoreClient mercadoPagoCoreClient;

    private final PreferenceClient preferenceClient = new PreferenceClient();

    public void setAccessTokenAndLog() {
        log.debug("\n\nProcessing PreferenceService.setAccessTokenAndLog\n\n");
        var accessToken = environment.getProperty("app.access-token");
        var configurationUrl = environment.getProperty("app.notification-url");
        log.debug("Configuration URL -> {}", configurationUrl);
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public PreferenceRequest buildPreferenceRequest(UMPreferenceMPDto umPreferenceMPDto,
                                                     TipoChequeraMercadoPagoCreditCardDto tipoChequeraContext) {
        log.debug("\n\nProcessing PreferenceCuotaService.buildPreferenceRequest\n\n");
        PreferenceItemRequest itemRequest = createItemRequest(umPreferenceMPDto);
        List<PreferenceItemRequest> itemRequests = List.of(itemRequest);

        PreferencePayerRequest payer = createPayerRequest(umPreferenceMPDto);
        String externalReference = createExternalReference(umPreferenceMPDto);
        PreferenceBackUrlsRequest backUrls = createBackUrlsRequest();
        PreferencePaymentMethodsRequest paymentMethods = createPaymentMethodsRequest(tipoChequeraContext);

        var fechaVencimientoMP = DateToolMP
                .convertToMPDate(umPreferenceMPDto.getMercadoPagoContext().getFechaVencimiento());

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(itemRequests)
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(externalReference)
                .notificationUrl(environment.getProperty("app.notification-url"))
                .expires(true)
                .expirationDateTo(fechaVencimientoMP)
                .paymentMethods(paymentMethods)
                .binaryMode(true)
                .statementDescriptor("UNIVMENDOZA")
                .build();

        log.debug("PreferenceRequest -> {}",
                Jsonifier.builder(preferenceRequest).build());
        return preferenceRequest;
    }

    private PreferenceItemRequest createItemRequest(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("\n\nProcessing PreferenceService.createItemRequest\n\n");
        if (umPreferenceMPDto.getChequeraCuota() == null) {
            // Configura Campaña
            return PreferenceItemRequest.builder()
                    .id("item-vacante-" + umPreferenceMPDto.getReservaVacante().getReservaVacanteId())
                    .title(umPreferenceMPDto.getReservaVacante().getCampanha().getNombre())
                    .quantity(1)
                    .unitPrice(umPreferenceMPDto.getMercadoPagoContext().getImporte())
                    .categoryId("others")
                    .currencyId("ARS")
                    .description(umPreferenceMPDto.getReservaVacante().getCampanha().getNombre())
                    .build();
        }
        return PreferenceItemRequest.builder()
                // Configura Cuota
                .id("item-key-" + umPreferenceMPDto.getChequeraCuota().getFacultadId() + "-"
                        + umPreferenceMPDto.getChequeraCuota().getTipoChequeraId() + "-"
                        + umPreferenceMPDto.getChequeraCuota().getChequeraSerieId() + "-"
                        + umPreferenceMPDto.getChequeraCuota().getProductoId() + "-"
                        + umPreferenceMPDto.getChequeraCuota().getAlternativaId() + "-"
                        + umPreferenceMPDto.getChequeraCuota().getCuotaId() + "-id-"
                        + umPreferenceMPDto.getChequeraCuota().getChequeraCuotaId())
                .title(umPreferenceMPDto.getChequeraCuota().getProducto().getNombre())
                .quantity(1)
                .unitPrice(umPreferenceMPDto.getMercadoPagoContext().getImporte())
                .categoryId("others")
                .currencyId("ARS")
                .description(umPreferenceMPDto.getChequeraCuota().getProducto().getNombre() + "-Periodo-"
                        + umPreferenceMPDto.getChequeraCuota().getMes() + "-"
                        + umPreferenceMPDto.getChequeraCuota().getAnho())
                .build();
    }

    private PreferencePayerRequest createPayerRequest(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("\n\nProcessing PreferenceService.createPayerRequest\n\n");
        if (umPreferenceMPDto.getChequeraCuota() == null) {
            // Configura Campaña
            return PreferencePayerRequest.builder()
                    .name(umPreferenceMPDto.getReservaVacante().getPersona().getNombre())
                    .surname(umPreferenceMPDto.getReservaVacante().getPersona().getApellido())
                    .email(umPreferenceMPDto.getReservaVacante().getDomicilio().getEmailPersonal())
                    .build();
        }
        // Configura Cuota
        return PreferencePayerRequest.builder()
                .name(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getPersona().getNombre())
                .surname(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getPersona().getApellido())
                .email(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getDomicilio().getEmailPersonal())
                .build();
    }

    private String createExternalReference(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("\n\nProcessing PreferenceService.createExternalReference\n\n");
        return String.format(umPreferenceMPDto.getReservaVacante().getReservaVacanteId()
                + "-" + umPreferenceMPDto.getMercadoPagoContext().getMercadoPagoContextId());
    }

    public PreferenceBackUrlsRequest createBackUrlsRequest() {
        log.debug("\n\nProcessing PreferenceService.createBackUrlsRequest\n\n");
        return PreferenceBackUrlsRequest.builder()
                .success("https://www.um.edu.ar")
                .pending("https://www.um.edu.ar")
                .failure("https://www.um.edu.ar")
                .build();
    }

    public PreferencePaymentMethodsRequest createPaymentMethodsRequest(
            TipoChequeraMercadoPagoCreditCardDto tipoChequeraContext) {
        log.debug("\n\nProcessing PreferenceService.createPaymentMethodsRequest\n\n");
        List<PreferencePaymentTypeRequest> excludedPaymentTypes = new ArrayList<>(List.of(
                PreferencePaymentTypeRequest.builder().id("ticket").build(),
                PreferencePaymentTypeRequest.builder().id("prepaid_card").build()));
        if (tipoChequeraContext == null) {
            excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("credit_card").build());
            return PreferencePaymentMethodsRequest.builder()
                    .excludedPaymentTypes(excludedPaymentTypes)
                    .installments(null)
                    .defaultInstallments(null)
                    .build();
        }
        return PreferencePaymentMethodsRequest.builder()
                .excludedPaymentTypes(excludedPaymentTypes)
                .installments(tipoChequeraContext.getInstallments())
                .defaultInstallments(tipoChequeraContext.getDefaultInstallments())
                .build();
    }

    public MercadoPagoContextDto createAndLogPreference(PreferenceRequest preferenceRequest,
                                                         MercadoPagoContextDto mercadoPagoContext) {
        log.debug("\n\nProcessing PreferenceService.createAndLogPreference\n\n");
        Preference preference = null;

        try {
            preference = preferenceClient.create(preferenceRequest);
            mercadoPagoContext.setInitPoint(preference.getInitPoint());
            mercadoPagoContext.setPreferenceId(preference.getId());
            mercadoPagoContext.setPreference(Jsonifier.builder(preference).build());
            mercadoPagoCoreClient.updateContext(mercadoPagoContext, mercadoPagoContext.getMercadoPagoContextId());
        } catch (MPApiException e) {
            log.error("MercadoPago API Error -> Status: {}, Message: {}, Response: {}",
                    e.getStatusCode(),
                    e.getMessage(),
                    e.getApiResponse() != null ? e.getApiResponse().getContent() : "No response content");
        } catch (MPException e) {
            log.error("MercadoPago General Error -> {}", e.getMessage());
        }

        return mercadoPagoContext;
    }

    public Preference retrievePreference(String preferenceId) {
        log.debug("\n\nProcessing PreferenceService.retrievePreference\n\n");
        // Obtener el access token y la URL de configuración del entorno
        var accessToken = environment.getProperty("app.access-token");

        // Configurar el token de acceso
        MercadoPagoConfig.setAccessToken(accessToken);

        // Crear el cliente de preferencias
        PreferenceClient preferenceClient = new PreferenceClient();
        Preference preference = null;

        try {
            // Realizar la solicitud GET para recuperar la preferencia
            preference = preferenceClient.get(preferenceId);
        } catch (MPException | MPApiException e) {
            log.debug("Error al recuperar la preferencia: {}", e.getMessage());
        }
        return preference;
    }

    public Preference retrievePreferenceById(String preferenceId) {
        log.debug("\n\nProcessing PreferenceService.retrievePreferenceById\n\n");
        try {
            return preferenceClient.get(preferenceId);
        } catch (MPException | MPApiException e) {
            log.debug("Error al recuperar la preferencia: {}", e.getMessage());
            return null;
        }
    }

    public void setAccessToken() {
        log.debug("\n\nProcessing PreferenceService.setAccessToken\n\n");
        var accessToken = environment.getProperty("app.access-token");
        log.debug("Access Token -> {}", accessToken);
        MercadoPagoConfig.setAccessToken(accessToken);
    }


}
