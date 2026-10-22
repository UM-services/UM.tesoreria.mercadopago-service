package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.CandidatoRenovacion;

import java.time.OffsetDateTime;
import java.util.List;

public interface ContextoRepository {

    ContextoConCuota crearContexto(Long chequeraCuotaId);

    List<ContextoConCuota> crearContextosChequera(Integer facultadId,
                                                  Integer tipoChequeraId,
                                                  Long chequeraSerieId,
                                                  Integer alternativaId);

    ContextoMercadoPago actualizar(ContextoMercadoPago contexto);

    List<ContextoMercadoPago> actualizarTodos(List<ContextoMercadoPago> contextos);

    List<Long> findActivosParaCambiar();

    List<CandidatoRenovacion> findRenovaciones(int lookbackDias, boolean reintentos,
                                               OffsetDateTime afterFecha, Long afterId, int size);

    void actualizarPreferencia(ContextoMercadoPago contexto);

    void marcarChanged(Long mercadoPagoContextId, byte changed);

}
