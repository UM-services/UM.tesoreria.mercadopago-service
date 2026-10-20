package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client.ContextoCoreClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client.ContextoQueryClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ChequeraCuotaDtoMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ContextoMercadoPagoDtoMapper;

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

    private ContextoConCuota toDomain(UMPreferenceMPDto dto) {
        if (dto == null) return null;
        return ContextoConCuota.builder()
                .contexto(contextoMapper.toDomain(dto.getMercadoPagoContext()))
                .cuota(cuotaMapper.toDomain(dto.getChequeraCuota()))
                .build();
    }

}
