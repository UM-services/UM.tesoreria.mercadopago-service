package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.ActualizarContextoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.ActualizarContextosUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.CrearContextoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.CrearContextosChequeraUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.FindRenovacionesUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.GetContextosActivosUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.UpdatePreferenciaContextoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.CandidatoRenovacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContextoService {

    private final CrearContextoUseCase crearContextoUseCase;
    private final CrearContextosChequeraUseCase crearContextosChequeraUseCase;
    private final ActualizarContextoUseCase actualizarContextoUseCase;
    private final ActualizarContextosUseCase actualizarContextosUseCase;
    private final GetContextosActivosUseCase getContextosActivosUseCase;
    private final FindRenovacionesUseCase findRenovacionesUseCase;
    private final UpdatePreferenciaContextoUseCase updatePreferenciaContextoUseCase;

    public ContextoConCuota crearContexto(Long chequeraCuotaId) {
        return crearContextoUseCase.crear(chequeraCuotaId);
    }

    public List<ContextoConCuota> crearContextosChequera(Integer facultadId,
                                                         Integer tipoChequeraId,
                                                         Long chequeraSerieId,
                                                         Integer alternativaId) {
        return crearContextosChequeraUseCase.crear(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

    public ContextoMercadoPago actualizar(ContextoMercadoPago contexto) {
        return actualizarContextoUseCase.actualizar(contexto);
    }

    public List<ContextoMercadoPago> actualizarTodos(List<ContextoMercadoPago> contextos) {
        return actualizarContextosUseCase.actualizar(contextos);
    }

    /**
     * @deprecated Barrido legado (90 días). Usar {@code findRenovaciones(...)}.
     */
    @Deprecated
    public List<Long> getActivosParaCambiar() {
        return getContextosActivosUseCase.getActivosParaCambiar();
    }

    public List<CandidatoRenovacion> findRenovaciones(int lookbackDias, boolean reintentos,
                                                     OffsetDateTime afterFecha, Long afterId, int size) {
        return findRenovacionesUseCase.findRenovaciones(lookbackDias, reintentos, afterFecha, afterId, size);
    }

    public void actualizarPreferencia(ContextoMercadoPago contexto) {
        updatePreferenciaContextoUseCase.actualizarPreferencia(contexto);
    }

    public void marcarChanged(Long mercadoPagoContextId, byte changed) {
        updatePreferenciaContextoUseCase.marcarChanged(mercadoPagoContextId, changed);
    }

}
