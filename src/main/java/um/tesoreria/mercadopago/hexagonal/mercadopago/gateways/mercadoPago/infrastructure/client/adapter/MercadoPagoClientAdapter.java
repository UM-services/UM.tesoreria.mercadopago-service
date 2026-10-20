package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.infrastructure.client.adapter;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.exception.MercadoPagoException;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.out.MercadoPagoGateway;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.infrastructure.client.mapper.PagoMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.infrastructure.client.mapper.PreferenciaMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.infrastructure.client.mapper.SolicitudPreferenciaMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoClientAdapter implements MercadoPagoGateway {

    private final PreferenceClient preferenceClient;
    private final PaymentClient paymentClient;
    private final SolicitudPreferenciaMapper solicitudPreferenciaMapper;
    private final PreferenciaMapper preferenciaMapper;
    private final PagoMapper pagoMapper;

    @Value("${app.access-token:}")
    private String accessToken;

    @Override
    public PreferenciaMercadoPago crear(SolicitudPreferencia solicitud) {
        MercadoPagoConfig.setAccessToken(accessToken);
        try {
            return preferenciaMapper.toDomain(
                    preferenceClient.create(solicitudPreferenciaMapper.toSdk(solicitud)));
        } catch (MPApiException e) {
            throw apiException(e);
        } catch (MPException e) {
            throw new MercadoPagoException(e.getMessage(), null, null, e);
        }
    }

    @Override
    public PreferenciaMercadoPago actualizar(String preferenceId, SolicitudPreferencia solicitud) {
        MercadoPagoConfig.setAccessToken(accessToken);
        try {
            return preferenciaMapper.toDomain(
                    preferenceClient.update(preferenceId, solicitudPreferenciaMapper.toSdk(solicitud)));
        } catch (MPApiException e) {
            throw apiException(e);
        } catch (MPException e) {
            throw new MercadoPagoException(e.getMessage(), null, null, e);
        }
    }

    @Override
    public Optional<PreferenciaMercadoPago> recuperar(String preferenceId) {
        MercadoPagoConfig.setAccessToken(accessToken);
        try {
            return Optional.ofNullable(preferenciaMapper.toDomain(preferenceClient.get(preferenceId)));
        } catch (MPException | MPApiException e) {
            log.debug("Error al recuperar la preferencia: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<PagoMercadoPago> recuperarPago(Long paymentId) {
        try {
            MPRequestOptions requestOptions = MPRequestOptions.builder()
                    .accessToken(accessToken)
                    .build();
            return Optional.ofNullable(pagoMapper.toDomain(paymentClient.get(paymentId, requestOptions)));
        } catch (MPException | MPApiException e) {
            log.error("Error getting payment for {}: {}", paymentId, e.getMessage());
            return Optional.empty();
        }
    }

    private MercadoPagoException apiException(MPApiException e) {
        return new MercadoPagoException(e.getMessage(), e.getStatusCode(),
                e.getApiResponse() != null ? e.getApiResponse().getContent() : null, e);
    }

}
