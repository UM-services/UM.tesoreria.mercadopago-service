package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.out;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

import java.util.Optional;

public interface MercadoPagoGateway {

    PreferenciaMercadoPago crear(SolicitudPreferencia solicitud);

    PreferenciaMercadoPago actualizar(String preferenceId, SolicitudPreferencia solicitud);

    Optional<PreferenciaMercadoPago> recuperar(String preferenceId);

    Optional<PagoMercadoPago> recuperarPago(Long paymentId);

}
