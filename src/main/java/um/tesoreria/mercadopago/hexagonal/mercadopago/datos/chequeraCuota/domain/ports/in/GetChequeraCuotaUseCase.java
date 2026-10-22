package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;

public interface GetChequeraCuotaUseCase {

    ChequeraCuota getById(Long chequeraCuotaId);

}
