package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in.ActualizarPreferenciaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.out.MercadoPagoGateway;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

@Component
@RequiredArgsConstructor
public class ActualizarPreferenciaUseCaseImpl implements ActualizarPreferenciaUseCase {

    private final MercadoPagoGateway mercadoPagoGateway;

    @Override
    public PreferenciaMercadoPago actualizar(String preferenceId, SolicitudPreferencia solicitud) {
        return mercadoPagoGateway.actualizar(preferenceId, solicitud);
    }

}
