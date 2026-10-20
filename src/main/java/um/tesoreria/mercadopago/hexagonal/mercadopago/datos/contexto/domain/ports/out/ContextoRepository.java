package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.util.List;

public interface ContextoRepository {

    ContextoConCuota crearContexto(Long chequeraCuotaId);

    List<ContextoConCuota> crearContextosChequera(Integer facultadId,
                                                  Integer tipoChequeraId,
                                                  Long chequeraSerieId,
                                                  Integer alternativaId);

    ContextoMercadoPago actualizar(ContextoMercadoPago contexto);

    List<ContextoMercadoPago> actualizarTodos(List<ContextoMercadoPago> contextos);

    List<Long> findActivosParaCambiar();

}
