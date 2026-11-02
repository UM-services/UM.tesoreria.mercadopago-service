package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagoMercadoPago;

import java.util.Optional;

public interface RecuperarPagoUseCase {

    Optional<PagoMercadoPago> recuperar(Long paymentId);

}
