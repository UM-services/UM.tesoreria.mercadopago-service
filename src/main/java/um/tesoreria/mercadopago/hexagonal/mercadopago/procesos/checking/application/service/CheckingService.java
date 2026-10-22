package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in.ChequearCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in.ChequearTodasUseCase;

@Service
@RequiredArgsConstructor
public class CheckingService {

    private final ChequearCuotaUseCase chequearCuotaUseCase;
    private final ChequearTodasUseCase chequearTodasUseCase;

    public String checkingCuota(Long chequeraCuotaId) {
        return chequearCuotaUseCase.chequear(chequeraCuotaId);
    }

    /**
     * @deprecated Barrido legado (90 días). Usar {@code POST /renovacion/run}.
     */
    @Deprecated
    public String checkingAllActive() {
        return chequearTodasUseCase.chequearTodas();
    }

}
