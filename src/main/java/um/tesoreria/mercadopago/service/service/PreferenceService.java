package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.mercadopago.MercadoPagoConfig;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.service.domain.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.service.util.DateToolMP;

import java.util.List;

@Service
@Slf4j
public class PreferenceService {

    private final Environment environment;
    private final MercadoPagoCoreClient mercadoPagoCoreClient;
    private final PreferenceClient preferenceClient;

    public PreferenceService(Environment environment,
                             MercadoPagoCoreClient mercadoPagoCoreClient) {
        this.environment = environment;
        this.mercadoPagoCoreClient = mercadoPagoCoreClient;
        this.preferenceClient = new PreferenceClient();
    }

    public String createPreference(Long chequeraCuotaId) {
        log.debug("Processing createPreference");
        var umPreferenceMPDto = mercadoPagoCoreClient.makeContext(chequeraCuotaId);
        if (umPreferenceMPDto == null) {
            return logAndReturnError(chequeraCuotaId);
        }

        logUmPreference(umPreferenceMPDto);

        var mercadoPagoContext = umPreferenceMPDto.getMercadoPagoContext();
        if (mercadoPagoContext != null && mercadoPagoContext.getInitPoint() != null) {
            if (mercadoPagoContext.getChanged() == 1) {
                mercadoPagoContext = updatePreference(mercadoPagoContext);
            }
            return " MercadoPagoContext -> " + logMercadoPagoContext(mercadoPagoContext);
        }

        setAccessTokenAndLog();

        PreferenceItemRequest itemRequest = createItemRequest(umPreferenceMPDto);
        List<PreferenceItemRequest> itemRequests = List.of(itemRequest);
        logItemRequests(itemRequests);

        PreferencePayerRequest payer = createPayerRequest(umPreferenceMPDto);
        String externalReference = createExternalReference(umPreferenceMPDto);
        PreferenceBackUrlsRequest backUrls = createBackUrlsRequest();
        PreferencePaymentMethodsRequest paymentMethods = createPaymentMethodsRequest();

        PreferenceRequest preferenceRequest = buildPreferenceRequest(itemRequests, payer, backUrls, externalReference, paymentMethods, umPreferenceMPDto);
        logPreferenceRequest(preferenceRequest);

        assert mercadoPagoContext != null;
        return createAndLogPreference(preferenceRequest, mercadoPagoContext, umPreferenceMPDto);
    }

    private PreferenceRequest buildPreferenceRequest(List<PreferenceItemRequest> itemRequests, PreferencePayerRequest payer, PreferenceBackUrlsRequest backUrls, String externalReference, PreferencePaymentMethodsRequest paymentMethods, UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("Processing buildPreferenceRequest");
        var fechaVencimientoMP = DateToolMP.convertToMPDate(umPreferenceMPDto.getMercadoPagoContext().getFechaVencimiento());
        return PreferenceRequest.builder()
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
    }

    public MercadoPagoContextDto updatePreference(MercadoPagoContextDto mercadoPagoContext) {
        log.debug("Processing updatePreference");
        setAccessToken();

        Preference preference = retrievePreferenceById(mercadoPagoContext.getPreferenceId());
        if (preference == null) return null;

        // Actualizar la fecha de vencimiento y el importe
        var fechaVencimientoMP = DateToolMP.convertToMPDate(mercadoPagoContext.getFechaVencimiento());
        var importe = mercadoPagoContext.getImporte();

        // Crea nuevo itemRequest con los valores actualizados
        var item = preference.getItems().getFirst();
        var itemRequest = PreferenceItemRequest.builder()
                .id(item.getId())
                .title(item.getTitle())
                .quantity(1)
                .unitPrice(importe)
                .categoryId("others")
                .currencyId("ARS")
                .description(item.getDescription())
                .build();
        List<PreferenceItemRequest> itemRequests = List.of(itemRequest);
        // Crea nuevo payerRequest con los valores actualizados
        var payer = preference.getPayer();
        var payerRequest = PreferencePayerRequest.builder()
                .name(payer.getName())
                .surname(payer.getSurname())
                .email(payer.getEmail())
                .build();

        // Crear un nuevo objeto PreferenceRequest con los valores actualizados
        PreferenceRequest updatedPreferenceRequest = PreferenceRequest.builder()
                .items(itemRequests) // Mantener los items existentes
                .payer(payerRequest) // Mantener el payer existente
                .backUrls(createBackUrlsRequest()) // Mantener las URLs de retorno
                .externalReference(preference.getExternalReference()) // Mantener la referencia externa
                .notificationUrl(preference.getNotificationUrl()) // Mantener la URL de notificación
                .expires(true)
                .expirationDateTo(fechaVencimientoMP) // Actualizar la fecha de vencimiento
                .paymentMethods(createPaymentMethodsRequest()) // Mantener los métodos de pago
                .binaryMode(preference.getBinaryMode()) // Mantener el modo binario
                .statementDescriptor(preference.getStatementDescriptor()) // Mantener el descriptor de estado
                .build();

        // Enviar la solicitud de actualización
        try {
            preference = preferenceClient.update(mercadoPagoContext.getPreferenceId(), updatedPreferenceRequest);
            log.debug("Preferencia actualizada con éxito");
            mercadoPagoContext.setChanged((byte) 0);
            mercadoPagoContext.setPreference(logPreference(preference));
            mercadoPagoContext = mercadoPagoCoreClient.updateContext(mercadoPagoContext, mercadoPagoContext.getMercadoPagoContextId());
        } catch (MPException | MPApiException e) {
            log.debug("Error al actualizar la preferencia: {}", e.getMessage());
        }

        return mercadoPagoContext; // Retornar el contexto actualizado
    }

    private String logAndReturnError(Long chequeraCuotaId) {
        log.debug("Processing logAndReturnError");
        log.error("No se encontro el MercadoPagoContext con el chequeraCuotaId: {}", chequeraCuotaId);
        return "No se encontro el MercadoPagoContext con el chequeraCuotaId: " + chequeraCuotaId;
    }

    private void logUmPreference(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("Processing logUmPreference");
        try {
            log.debug("UMPreferenceMPDto -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(umPreferenceMPDto));
        } catch (JsonProcessingException e) {
            log.debug("UMPreferenceMPDto Error -> {}", e.getMessage());
        }
    }

    private String logMercadoPagoContext(MercadoPagoContextDto mercadoPagoContext) {
        log.debug("Processing logMercadoPagoContext");
        String mercadoPagoContextString = "";
        try {
            mercadoPagoContextString = JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(mercadoPagoContext);
        } catch (JsonProcessingException e) {
            log.debug("MercadoPagoContext Error -> {}", e.getMessage());
        }
        return mercadoPagoContextString;
    }

    private void setAccessTokenAndLog() {
        log.debug("Processing setAccessTokenAndLog");
        var accessToken = environment.getProperty("app.access-token");
        log.debug("Access Token -> {}", accessToken);
        var configurationUrl = environment.getProperty("app.notification-url");
        log.debug("Configuration URL -> {}", configurationUrl);
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    private PreferenceItemRequest createItemRequest(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("Processing createItemRequest");
        return PreferenceItemRequest.builder()
                .id("item-key-" + umPreferenceMPDto.getChequeraCuota().getFacultadId() + "-" + umPreferenceMPDto.getChequeraCuota().getTipoChequeraId() + "-" + umPreferenceMPDto.getChequeraCuota().getChequeraSerieId() + "-" + umPreferenceMPDto.getChequeraCuota().getProductoId() + "-" + umPreferenceMPDto.getChequeraCuota().getAlternativaId() + "-" + umPreferenceMPDto.getChequeraCuota().getCuotaId() + "-id-" + umPreferenceMPDto.getChequeraCuota().getChequeraCuotaId())
                .title(umPreferenceMPDto.getChequeraCuota().getProducto().getNombre())
                .quantity(1)
                .unitPrice(umPreferenceMPDto.getMercadoPagoContext().getImporte())
                .categoryId("others")
                .currencyId("ARS")
                .description(umPreferenceMPDto.getChequeraCuota().getProducto().getNombre() + "-Periodo-" + umPreferenceMPDto.getChequeraCuota().getMes() + "-" + umPreferenceMPDto.getChequeraCuota().getAnho())
                .build();
    }

    private void logItemRequests(List<PreferenceItemRequest> itemRequests) {
        log.debug("Processing logItemRequests");
        try {
            log.debug("ItemRequests -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(itemRequests));
        } catch (JsonProcessingException e) {
            log.debug("Item Requests Error -> {}", e.getMessage());
        }
    }

    private PreferencePayerRequest createPayerRequest(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("Processing createPayerRequest");
        return PreferencePayerRequest.builder()
                .name(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getPersona().getNombre())
                .surname(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getPersona().getApellido())
                .email(umPreferenceMPDto.getChequeraCuota().getChequeraSerie().getDomicilio().getEmailPersonal())
                .build();
    }

    private String createExternalReference(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("Processing createExternalReference");
        return String.format("%02d", umPreferenceMPDto.getChequeraCuota().getFacultadId())
                + String.format("%03d", umPreferenceMPDto.getChequeraCuota().getTipoChequeraId())
                + String.format("%05d", umPreferenceMPDto.getChequeraCuota().getChequeraSerieId())
                + String.format("%02d", umPreferenceMPDto.getChequeraCuota().getProductoId())
                + String.format("%02d", umPreferenceMPDto.getChequeraCuota().getAlternativaId())
                + String.format("%04d", umPreferenceMPDto.getChequeraCuota().getCuotaId())
                + "-" + umPreferenceMPDto.getChequeraCuota().getChequeraCuotaId()
                + "-" + umPreferenceMPDto.getMercadoPagoContext().getMercadoPagoContextId();
    }

    private PreferenceBackUrlsRequest createBackUrlsRequest() {
        log.debug("Processing createBackUrlsRequest");
        return PreferenceBackUrlsRequest.builder()
                .success("https://www.um.edu.ar")
                .pending("https://www.um.edu.ar")
                .failure("https://www.um.edu.ar")
                .build();
    }

    private PreferencePaymentMethodsRequest createPaymentMethodsRequest() {
        log.debug("Processing createPaymentMethodsRequest");
        List<PreferencePaymentTypeRequest> excludedPaymentTypes = List.of(
                PreferencePaymentTypeRequest.builder().id("credit_card").build(),
                PreferencePaymentTypeRequest.builder().id("ticket").build()
        );
        return PreferencePaymentMethodsRequest.builder()
                .excludedPaymentTypes(excludedPaymentTypes)
                .build();
    }

    private void logPreferenceRequest(PreferenceRequest preferenceRequest) {
        log.debug("Processing logPreferenceRequest");
        try {
            log.debug("PreferenceRequest -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(preferenceRequest));
        } catch (JsonProcessingException e) {
            log.debug("Preference Request Error -> {}", e.getMessage());
        }
    }

    private String createAndLogPreference(PreferenceRequest preferenceRequest, MercadoPagoContextDto mercadoPagoContext, UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("Processing createAndLogPreference");
        Preference preference = null;

        try {
            preference = preferenceClient.create(preferenceRequest);
            mercadoPagoContext.setInitPoint(preference.getInitPoint());
            mercadoPagoContext.setPreferenceId(preference.getId());
            mercadoPagoContext.setPreference(logPreference(preference));
            mercadoPagoCoreClient.updateContext(mercadoPagoContext, mercadoPagoContext.getMercadoPagoContextId());
        } catch (MPException | MPApiException e) {
            log.debug("MercadoPago Error -> {}", e.getMessage());
        }

        return logFinalPreference(preference, mercadoPagoContext);
    }

    private String logFinalPreference(Preference preference, MercadoPagoContextDto mercadoPagoContext) {
        log.debug("Processing logFinalPreference");
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

    public Preference retrievePreference(String preferenceId) {
        log.debug("Processing retrievePreference");
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
            logPreference(preference);
        } catch (MPException | MPApiException e) {
            log.debug("Error al recuperar la preferencia: {}", e.getMessage());
        }
        return preference;
    }

    private String logPreference(Preference preference) {
        log.debug("Processing logPreference");
        try {
            var preferenceString = JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(preference);
            log.debug("Preference -> {}", preferenceString);
            return preferenceString;
        } catch (JsonProcessingException e) {
            log.debug("Preference Error -> {}", e.getMessage());
        }
        return "";
    }

    private Preference retrievePreferenceById(String preferenceId) {
        log.debug("Processing retrievePreferenceById");
        try {
            return preferenceClient.get(preferenceId);
        } catch (MPException | MPApiException e) {
            log.debug("Error al recuperar la preferencia: {}", e.getMessage());
            return null;
        }
    }

    private void setAccessToken() {
        log.debug("Processing setAccessToken");
        var accessToken = environment.getProperty("app.access-token");
        log.debug("Access Token -> {}", accessToken);
        MercadoPagoConfig.setAccessToken(accessToken);
    }

}
