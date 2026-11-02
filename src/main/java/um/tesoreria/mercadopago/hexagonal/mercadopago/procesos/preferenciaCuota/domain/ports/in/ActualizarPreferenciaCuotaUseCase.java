package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

public interface ActualizarPreferenciaCuotaUseCase {

    ContextoMercadoPago actualizar(ContextoMercadoPago contexto);

    /**
     * Variante que evita volver a pedir la cuota al core cuando el llamador ya la tiene.
     */
    ContextoMercadoPago actualizar(ContextoMercadoPago contexto, ChequeraCuota cuota);

}
