package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.util.List;

public interface ActualizarContextosUseCase {

    List<ContextoMercadoPago> actualizar(List<ContextoMercadoPago> contextos);

}
