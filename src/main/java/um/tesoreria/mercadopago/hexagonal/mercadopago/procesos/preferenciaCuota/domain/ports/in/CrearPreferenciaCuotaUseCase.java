package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

public interface CrearPreferenciaCuotaUseCase {

    ContextoMercadoPago crear(ContextoMercadoPago contexto, ChequeraCuota cuota);

}
