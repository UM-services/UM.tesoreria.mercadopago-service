package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;

import java.util.List;

public interface CrearContextosChequeraBulkUseCase {

    List<ContextoConCuota> crear(Integer facultadId,
                                 Integer tipoChequeraId,
                                 Long chequeraSerieId,
                                 Integer alternativaId);

}
