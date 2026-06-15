package um.tesoreria.mercadopago.hexagonal.preference.cuota.application.service;

import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.mercadopago.MercadoPagoConfig;
import um.tesoreria.mercadopago.client.core.ChequeraCuotaClient;
import um.tesoreria.mercadopago.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.client.core.TipoChequeraMercadoPagoCreditCardClient;
import um.tesoreria.mercadopago.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.domain.dto.TipoChequeraMercadoPagoCreditCardDto;
import um.tesoreria.mercadopago.domain.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.preference.service.PreferenceService;
import um.tesoreria.mercadopago.util.DateToolMP;
import um.tesoreria.mercadopago.util.Jsonifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceCuotaService {

    private final PreferenceService preferenceService;

    private final PreferenceClient preferenceClient = new PreferenceClient();
    private final TipoChequeraMercadoPagoCreditCardClient tipoChequeraMercadoPagoCreditCardClient;
    private final ChequeraCuotaClient chequeraCuotaClient;

    public MercadoPagoContextDto createPreference(UMPreferenceMPDto umPreferenceMPDto) {
        log.debug("\n\nProcessing PreferenceCuotaService.createPreference\n\n");
        if (umPreferenceMPDto == null) {
            throw new IllegalArgumentException("UMPreferenceMPDto cannot be null");
        }
        log.debug("UMPreferenceMP -> {}", umPreferenceMPDto.jsonify());
        var mercadoPagoContext = umPreferenceMPDto.getMercadoPagoContext();
        if (mercadoPagoContext != null && mercadoPagoContext.getInitPoint() != null) {
            if (mercadoPagoContext.getChanged() == 1) {
                mercadoPagoContext = updatePreference(mercadoPagoContext);
            }
            return mercadoPagoContext;
        }
        var chequeraCuota = umPreferenceMPDto.getChequeraCuota();
        var tipoChequeraContext = getTipoChequeraContext(chequeraCuota.getTipoChequeraId(),
                chequeraCuota.getAlternativaId());
        preferenceService.setAccessTokenAndLog();
        PreferenceRequest preferenceRequest = preferenceService.buildPreferenceRequest(umPreferenceMPDto, tipoChequeraContext);
        assert mercadoPagoContext != null;
        return preferenceService.createAndLogPreference(preferenceRequest, mercadoPagoContext);
    }

    public MercadoPagoContextDto updatePreference(MercadoPagoContextDto mercadoPagoContext) {
        log.debug("\n\nProcessing PreferenceCuotaService.updatePreference\n\n");
        preferenceService.setAccessToken();

        Preference preference = preferenceService.retrievePreferenceById(mercadoPagoContext.getPreferenceId());
        if (preference == null)
            return null;

        // Actualizar la fecha de vencimiento y el importe
        var fechaVencimientoMP = DateToolMP.convertToMPDate(mercadoPagoContext.getFechaVencimiento());
        var importe = mercadoPagoContext.getImporte();

        var chequeraCuota = chequeraCuotaClient.findByChequeraCuotaId(mercadoPagoContext.getChequeraCuotaId());
        var tipoChequeraContext = getTipoChequeraContext(chequeraCuota.getTipoChequeraId(),
                chequeraCuota.getAlternativaId());

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

        var paymentMethods = preferenceService.createPaymentMethodsRequest(tipoChequeraContext);
         log.debug("PaymentMethods -> {}", Jsonifier.builder(paymentMethods).build());
        // Crear un nuevo objeto PreferenceRequest con los valores actualizados
        PreferenceRequest updatedPreferenceRequest = PreferenceRequest.builder()
                .items(itemRequests)
                .payer(payerRequest)
                .backUrls(preferenceService.createBackUrlsRequest())
                .externalReference(preference.getExternalReference())
                .notificationUrl(preference.getNotificationUrl())
                .expires(true)
                .expirationDateTo(fechaVencimientoMP)
                .paymentMethods(paymentMethods)
                .binaryMode(preference.getBinaryMode())
                .statementDescriptor(preference.getStatementDescriptor())
                .build();

        try {
            preference = preferenceClient.update(mercadoPagoContext.getPreferenceId(), updatedPreferenceRequest);
            log.debug("\n\nPreferencia actualizada con éxito\n\n");
            mercadoPagoContext.setChanged((byte) 0);
            mercadoPagoContext.setPreference(Jsonifier.builder(preference).build());
            mercadoPagoContext.setLastVencimientoUpdated(OffsetDateTime.now(ZoneOffset.UTC));
            // mercadoPagoContext = mercadoPagoCoreClient.updateContext(mercadoPagoContext,
            // mercadoPagoContext.getMercadoPagoContextId());
        } catch (MPException | MPApiException e) {
            log.debug("\n\nError al actualizar la preferencia: {}\n\n", e.getMessage());
        }

        return mercadoPagoContext;
    }

    private TipoChequeraMercadoPagoCreditCardDto getTipoChequeraContext(Integer tipoChequeraId, Integer alternativaId) {
        try {
            var tipoChequeraContext = tipoChequeraMercadoPagoCreditCardClient.findByUnique(tipoChequeraId,
                    alternativaId);
            if (tipoChequeraContext.getActive() == 0) {
                return null;
            }
            if (Objects.equals(tipoChequeraContext.getAlternativaId(), alternativaId)) {
                return tipoChequeraContext;
            }
            return null;
        } catch (Exception e) {
            log.debug("Error al obtener el contexto de tipo de chequera: {}", e.getMessage());
            return null;
        }
    }

}
