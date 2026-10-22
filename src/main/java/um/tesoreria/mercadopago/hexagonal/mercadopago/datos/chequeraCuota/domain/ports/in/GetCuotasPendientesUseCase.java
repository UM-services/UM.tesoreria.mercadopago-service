package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;

import java.util.List;

public interface GetCuotasPendientesUseCase {

    List<ChequeraCuota> getPendientes(Integer facultadId,
                                      Integer tipoChequeraId,
                                      Long chequeraSerieId,
                                      Integer alternativaId);

}
