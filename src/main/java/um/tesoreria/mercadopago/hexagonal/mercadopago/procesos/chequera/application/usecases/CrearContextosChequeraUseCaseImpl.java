package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.service.ChequeraCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.domain.ports.in.CrearContextosChequeraUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrearContextosChequeraUseCaseImpl implements CrearContextosChequeraUseCase {

    private final ChequeraCuotaService chequeraCuotaService;
    private final ContextoService contextoService;
    private final PreferenciaCuotaService preferenciaCuotaService;

    @Override
    public List<ContextoConCuota> crear(Integer facultadId,
                                        Integer tipoChequeraId,
                                        Long chequeraSerieId,
                                        Integer alternativaId) {
        log.debug("\n\nProcessing CrearContextosChequeraUseCaseImpl.crear\n\n");
        List<ContextoConCuota> preferences = new ArrayList<>();
        for (var cuota : chequeraCuotaService.getPendientes(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)) {
            var contextoConCuota = contextoService.crearContexto(cuota.getChequeraCuotaId());
            if (contextoConCuota != null) {
                var contexto = preferenciaCuotaService.crear(contextoConCuota.getContexto(), contextoConCuota.getCuota());
                if (contexto != null) {
                    try {
                        contextoService.actualizar(contexto);
                    } catch (Exception e) {
                        log.error("Error updating context in Core: {}", e.getMessage());
                    }
                    preferences.add(ContextoConCuota.builder()
                            .contexto(contexto)
                            .cuota(cuota)
                            .build());
                }
            }
        }
        return preferences;
    }

}
