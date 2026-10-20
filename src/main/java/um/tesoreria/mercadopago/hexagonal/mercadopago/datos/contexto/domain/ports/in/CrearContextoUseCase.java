package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;

public interface CrearContextoUseCase {

    ContextoConCuota crear(Long chequeraCuotaId);

}
