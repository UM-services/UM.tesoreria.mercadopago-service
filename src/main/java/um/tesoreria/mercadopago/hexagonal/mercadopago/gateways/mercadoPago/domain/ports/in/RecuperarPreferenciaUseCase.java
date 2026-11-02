package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;

import java.util.Optional;

public interface RecuperarPreferenciaUseCase {

    Optional<PreferenciaMercadoPago> recuperar(String preferenceId);

}
