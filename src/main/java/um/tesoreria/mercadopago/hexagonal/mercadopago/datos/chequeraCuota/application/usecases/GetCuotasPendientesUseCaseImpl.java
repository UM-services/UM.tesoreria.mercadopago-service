package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.in.GetCuotasPendientesUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.out.ChequeraCuotaRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetCuotasPendientesUseCaseImpl implements GetCuotasPendientesUseCase {

    private final ChequeraCuotaRepository chequeraCuotaRepository;

    @Override
    public List<ChequeraCuota> getPendientes(Integer facultadId,
                                             Integer tipoChequeraId,
                                             Long chequeraSerieId,
                                             Integer alternativaId) {
        return chequeraCuotaRepository.findPendientes(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

}
