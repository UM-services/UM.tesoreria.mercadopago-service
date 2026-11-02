package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.CandidatoRenovacion;

import java.time.OffsetDateTime;
import java.util.List;

public interface FindRenovacionesUseCase {

    List<CandidatoRenovacion> findRenovaciones(int lookbackDias, boolean reintentos,
                                               OffsetDateTime afterFecha, Long afterId, int size);

}
