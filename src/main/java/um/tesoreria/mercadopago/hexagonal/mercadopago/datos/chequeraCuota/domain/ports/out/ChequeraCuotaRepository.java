package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.out;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;

import java.util.List;
import java.util.Optional;

public interface ChequeraCuotaRepository {

    List<ChequeraCuota> findPendientes(Integer facultadId,
                                      Integer tipoChequeraId,
                                      Long chequeraSerieId,
                                      Integer alternativaId);

    Optional<ChequeraCuota> findByChequeraCuotaId(Long chequeraCuotaId);

}
