package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in.ChequearCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in.ChequearTodasUseCase;

/**
 * @deprecated Barrido legado (90 días). Usar {@code RenovarVencimientosUseCaseImpl}.
 */
@Deprecated
@Component
@RequiredArgsConstructor
@Slf4j
public class ChequearTodasUseCaseImpl implements ChequearTodasUseCase {

    private final ContextoService contextoService;
    private final ChequearCuotaUseCase chequearCuotaUseCase;

    @Override
    public String chequearTodas() {
        log.debug("Processing ChequearTodasUseCaseImpl.chequearTodas");

        int procesados = 0;
        int omitidos = 0;
        int fallidos = 0;

        for (var chequeraCuotaId : contextoService.getActivosParaCambiar()) {
            // Los contextos de reserva de vacante no tienen cuota: antes abortaban la corrida.
            if (chequeraCuotaId == null) {
                omitidos++;
                continue;
            }
            try {
                chequearCuotaUseCase.chequear(chequeraCuotaId);
                procesados++;
            } catch (Exception e) {
                // Un item con problemas no puede cortar el barrido completo.
                fallidos++;
                log.warn("Falló el checking de la cuota {}: {}", chequeraCuotaId, e.getMessage());
            }
        }

        log.info("Checking finalizado -> procesados: {}, omitidos: {}, fallidos: {}",
                procesados, omitidos, fallidos);
        return "Checked";
    }

}
