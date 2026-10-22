package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.domain.ports.in.CrearContextosChequeraBulkUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrearContextosChequeraBulkUseCaseImpl implements CrearContextosChequeraBulkUseCase {

    private final ContextoService contextoService;
    private final PreferenciaCuotaService preferenciaCuotaService;

    @Override
    public List<ContextoConCuota> crear(Integer facultadId,
                                        Integer tipoChequeraId,
                                        Long chequeraSerieId,
                                        Integer alternativaId) {
        log.debug("\n\nProcessing CrearContextosChequeraBulkUseCaseImpl.crear\n\n");
        var coreContexts = contextoService.crearContextosChequera(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
        List<ContextoConCuota> preferences = new ArrayList<>();
        List<ContextoMercadoPago> contextsToUpdate = new ArrayList<>();

        for (var contextoConCuota : coreContexts) {
            var contexto = preferenciaCuotaService.crear(contextoConCuota.getContexto(), contextoConCuota.getCuota());
            if (contexto != null) {
                contextsToUpdate.add(contexto);
                preferences.add(ContextoConCuota.builder()
                        .contexto(contexto)
                        .cuota(contextoConCuota.getCuota())
                        .build());
            }
        }

        if (!contextsToUpdate.isEmpty()) {
            try {
                contextoService.actualizarTodos(contextsToUpdate);
            } catch (Exception e) {
                log.error("Error updating contexts in Core in bulk: {}", e.getMessage());
            }
        }

        return preferences;
    }

}
