package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in;

import java.util.List;

public interface GetContextosActivosUseCase {

    List<Long> getActivosParaCambiar();

}
