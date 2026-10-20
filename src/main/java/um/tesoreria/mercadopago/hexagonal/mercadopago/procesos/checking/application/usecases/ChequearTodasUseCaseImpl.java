package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in.ChequearCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in.ChequearTodasUseCase;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChequearTodasUseCaseImpl implements ChequearTodasUseCase {

    private final ContextoService contextoService;
    private final ChequearCuotaUseCase chequearCuotaUseCase;

    @Override
    public String chequearTodas() {
        log.debug("Processing ChequearTodasUseCaseImpl.chequearTodas");
        for (var chequeraCuotaId : contextoService.getActivosParaCambiar()) {
            log.debug(chequearCuotaUseCase.chequear(chequeraCuotaId));
        }
        return "Checked";
    }

}
