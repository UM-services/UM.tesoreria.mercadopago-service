package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client.ContextoCoreClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client.ContextoQueryClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.CandidatoRenovacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.PreferenciaPatchDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.RenovacionesCandidatosRequestDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ChequeraCuotaDtoMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ContextoMercadoPagoDtoMapper;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContextoClientAdapter implements ContextoRepository {

    private final ContextoCoreClient contextoCoreClient;
    private final ContextoQueryClient contextoQueryClient;
    private final ContextoMercadoPagoDtoMapper contextoMapper;
    private final ChequeraCuotaDtoMapper cuotaMapper;

    @Override
    public ContextoConCuota crearContexto(Long chequeraCuotaId) {
        return toDomain(contextoCoreClient.makeContext(chequeraCuotaId));
    }

    @Override
    public List<ContextoConCuota> crearContextosChequera(Integer facultadId,
                                                         Integer tipoChequeraId,
                                                         Long chequeraSerieId,
                                                         Integer alternativaId) {
        return contextoCoreClient.makeContextsChequera(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public ContextoMercadoPago actualizar(ContextoMercadoPago contexto) {
        MercadoPagoContextDto request = contextoMapper.toDto(contexto);
        return contextoMapper.toDomain(
                contextoCoreClient.updateContext(request, contexto.getMercadoPagoContextId()));
    }

    @Override
    public List<ContextoMercadoPago> actualizarTodos(List<ContextoMercadoPago> contextos) {
        List<MercadoPagoContextDto> requests = contextos.stream()
                .map(contextoMapper::toDto)
                .toList();
        return contextoCoreClient.updateContexts(requests).stream()
                .map(contextoMapper::toDomain)
                .toList();
    }

    @Override
    public List<Long> findActivosParaCambiar() {
        return contextoQueryClient.findAllActiveToChange();
    }

    @Override
    public List<CandidatoRenovacion> findRenovaciones(int lookbackDias, boolean reintentos,
                                                     OffsetDateTime afterFecha, Long afterId, int size) {
        var request = RenovacionesCandidatosRequestDto.builder()
                .lookbackDias(lookbackDias)
                .reintentos(reintentos)
                .afterFecha(afterFecha)
                .afterId(afterId)
                .size(size)
                .build();
        return contextoCoreClient.findRenovaciones(request)
                .stream()
                .map(this::toCandidato)
                .toList();
    }

    @Override
    public void actualizarPreferencia(ContextoMercadoPago contexto) {
        PreferenciaPatchDto patch = PreferenciaPatchDto.builder()
                .fechaVencimiento(contexto.getFechaVencimiento())
                .importe(contexto.getImporte())
                .initPoint(contexto.getInitPoint())
                .preferenceId(contexto.getPreferenceId())
                .preference(contexto.getPreference())
                .changed(contexto.getChanged())
                .lastVencimientoUpdated(contexto.getLastVencimientoUpdated())
                .build();
        contextoCoreClient.updatePreferencia(patch, contexto.getMercadoPagoContextId());
    }

    @Override
    public void marcarChanged(Long mercadoPagoContextId, byte changed) {
        var patch = PreferenciaPatchDto.builder().changed(changed).build();
        contextoCoreClient.updatePreferencia(patch, mercadoPagoContextId);
    }

    private CandidatoRenovacion toCandidato(UMPreferenceMPDto dto) {
        if (dto == null) {
            return null;
        }
        return new CandidatoRenovacion(
                contextoMapper.toDomain(dto.getMercadoPagoContext()),
                cuotaMapper.toDomain(dto.getChequeraCuota()),
                dto.getFechaVencimientoDestino(),
                dto.getImporteDestino());
    }

    private ContextoConCuota toDomain(UMPreferenceMPDto dto) {
        if (dto == null) return null;
        return ContextoConCuota.builder()
                .contexto(contextoMapper.toDomain(dto.getMercadoPagoContext()))
                .cuota(cuotaMapper.toDomain(dto.getChequeraCuota()))
                .build();
    }

}
