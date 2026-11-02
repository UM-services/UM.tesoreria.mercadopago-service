package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ReservaVacante;

public interface CrearPreferenciaVacanteUseCase {

    ContextoMercadoPago crear(ContextoMercadoPago contexto, ReservaVacante reserva);

}
