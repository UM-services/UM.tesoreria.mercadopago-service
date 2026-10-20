package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in.RecuperarPreferenciaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.out.MercadoPagoGateway;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecuperarPreferenciaUseCaseImpl implements RecuperarPreferenciaUseCase {

    private final MercadoPagoGateway mercadoPagoGateway;

    @Override
    public Optional<PreferenciaMercadoPago> recuperar(String preferenceId) {
        return mercadoPagoGateway.recuperar(preferenceId);
    }

}
