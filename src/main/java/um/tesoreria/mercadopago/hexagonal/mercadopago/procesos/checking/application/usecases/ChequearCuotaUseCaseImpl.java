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
        if (chequeraCuotaId == null) {
            log.debug("chequear() recibió un id nulo: se omite");
            return "Checked";
        }

        var contextoConCuota = contextoService.crearContexto(chequeraCuotaId);
        if (contextoConCuota == null || contextoConCuota.getContexto() == null) {
            log.debug("Sin contexto para chequear la cuota {}", chequeraCuotaId);
            return "Checked";
        }

        var contexto = contextoConCuota.getContexto();
        // Solo se persiste si la preferencia es nueva o si el core detectó un cambio de vencimiento.
        boolean requierePersistencia = contexto.getInitPoint() == null
                || Byte.valueOf((byte) 1).equals(contexto.getChanged());

        var resultado = preferenciaCuotaService.crear(contexto, contextoConCuota.getCuota());
        if (requierePersistencia && resultado != null && resultado.getInitPoint() != null) {
            try {
                contextoService.actualizar(resultado);
            } catch (Exception e) {
                log.error("Error updating context in Core: {}", e.getMessage());
            }
        }
        return "Checked";
    }

}
