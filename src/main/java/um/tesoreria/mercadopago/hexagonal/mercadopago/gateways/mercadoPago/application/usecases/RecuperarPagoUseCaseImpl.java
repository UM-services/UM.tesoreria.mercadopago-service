package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagoMercadoPago;

import java.util.Optional;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in.RecuperarPagoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.out.MercadoPagoGateway;

@Component
@RequiredArgsConstructor
public class RecuperarPagoUseCaseImpl implements RecuperarPagoUseCase {

    private final MercadoPagoGateway mercadoPagoGateway;

    @Override
    public Optional<PagoMercadoPago> recuperar(Long paymentId) {
        return mercadoPagoGateway.recuperarPago(paymentId);
    }

}
