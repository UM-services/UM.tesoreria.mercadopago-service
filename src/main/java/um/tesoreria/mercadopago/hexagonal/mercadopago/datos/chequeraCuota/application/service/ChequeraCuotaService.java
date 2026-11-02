package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.in.GetChequeraCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.in.GetCuotasPendientesUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChequeraCuotaService {

    private final GetCuotasPendientesUseCase getCuotasPendientesUseCase;
    private final GetChequeraCuotaUseCase getChequeraCuotaUseCase;

    public List<ChequeraCuota> getPendientes(Integer facultadId,
                                             Integer tipoChequeraId,
                                             Long chequeraSerieId,
                                             Integer alternativaId) {
        return getCuotasPendientesUseCase.getPendientes(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

    public ChequeraCuota getById(Long chequeraCuotaId) {
        return getChequeraCuotaUseCase.getById(chequeraCuotaId);
    }

}
