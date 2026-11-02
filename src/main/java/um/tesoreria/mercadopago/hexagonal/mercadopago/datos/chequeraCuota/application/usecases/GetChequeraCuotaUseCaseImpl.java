package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.exception.ChequeraCuotaException;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.in.GetChequeraCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.out.ChequeraCuotaRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;

@Component
@RequiredArgsConstructor
public class GetChequeraCuotaUseCaseImpl implements GetChequeraCuotaUseCase {

    private final ChequeraCuotaRepository chequeraCuotaRepository;

    @Override
    public ChequeraCuota getById(Long chequeraCuotaId) {
        return chequeraCuotaRepository.findByChequeraCuotaId(chequeraCuotaId)
                .orElseThrow(() -> new ChequeraCuotaException(chequeraCuotaId));
    }

}
