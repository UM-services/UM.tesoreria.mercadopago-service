package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in.CrearPreferenciaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.out.MercadoPagoGateway;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

@Component
@RequiredArgsConstructor
public class CrearPreferenciaUseCaseImpl implements CrearPreferenciaUseCase {

    private final MercadoPagoGateway mercadoPagoGateway;

    @Override
    public PreferenciaMercadoPago crear(SolicitudPreferencia solicitud) {
        return mercadoPagoGateway.crear(solicitud);
    }

}
