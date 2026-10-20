package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in.ChequearCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChequearCuotaUseCaseImpl implements ChequearCuotaUseCase {

    private final ContextoService contextoService;
    private final PreferenciaCuotaService preferenciaCuotaService;

    @Override
    public String chequear(Long chequeraCuotaId) {
        log.debug("Processing ChequearCuotaUseCaseImpl.chequear -> {}", chequeraCuotaId);
        var contextoConCuota = contextoService.crearContexto(chequeraCuotaId);
        if (contextoConCuota != null) {
            var contexto = preferenciaCuotaService.crear(contextoConCuota.getContexto(), contextoConCuota.getCuota());
            if (contexto != null) {
                try {
                    contextoService.actualizar(contexto);
                } catch (Exception e) {
                    log.error("Error updating context in Core: {}", e.getMessage());
                }
            }
        }
        return "Checked";
    }

}
