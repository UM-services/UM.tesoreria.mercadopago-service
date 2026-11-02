package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

public interface ActualizarContextoUseCase {

    ContextoMercadoPago actualizar(ContextoMercadoPago contexto);

}
