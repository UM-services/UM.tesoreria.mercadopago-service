package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.infrastructure.client.mapper;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferencePaymentMethodsRequest;
import com.mercadopago.client.preference.PreferencePaymentTypeRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

import java.util.List;

@Component
public class SolicitudPreferenciaMapper {

    public PreferenceRequest toSdk(SolicitudPreferencia solicitud) {
        if (solicitud == null) return null;

        List<PreferenceItemRequest> items = solicitud.getItems().stream()
                .map(item -> PreferenceItemRequest.builder()
                        .id(item.getId())
                        .title(item.getTitulo())
                        .quantity(item.getCantidad())
                        .unitPrice(item.getPrecioUnitario())
                        .categoryId(item.getCategoria())
                        .currencyId(item.getMoneda())
                        .description(item.getDescripcion())
                        .build())
                .toList();

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(solicitud.getPagador().getNombre())
                .surname(solicitud.getPagador().getApellido())
                .email(solicitud.getPagador().getEmail())
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(solicitud.getUrlsRetorno().getExito())
                .pending(solicitud.getUrlsRetorno().getPendiente())
                .failure(solicitud.getUrlsRetorno().getFallo())
                .build();

        List<PreferencePaymentTypeRequest> excluded = solicitud.getMediosPago().getTiposExcluidos().stream()
                .map(id -> PreferencePaymentTypeRequest.builder().id(id).build())
                .toList();

        PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                .excludedPaymentTypes(excluded)
                .installments(solicitud.getMediosPago().getCuotas())
                .defaultInstallments(solicitud.getMediosPago().getCuotasPorDefecto())
                .build();

        return PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(solicitud.getReferenciaExterna())
                .notificationUrl(solicitud.getUrlNotificacion())
                .expires(solicitud.getExpira())
                .expirationDateTo(solicitud.getFechaVencimiento())
                .paymentMethods(paymentMethods)
                .binaryMode(solicitud.getModoBinario())
                .statementDescriptor(solicitud.getDescripcionExtracto())
                .build();
    }

}
