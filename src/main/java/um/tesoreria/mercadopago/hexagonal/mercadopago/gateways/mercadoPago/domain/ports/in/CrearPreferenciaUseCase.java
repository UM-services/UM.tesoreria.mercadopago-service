package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

public interface CrearPreferenciaUseCase {

    PreferenciaMercadoPago crear(SolicitudPreferencia solicitud);

}
