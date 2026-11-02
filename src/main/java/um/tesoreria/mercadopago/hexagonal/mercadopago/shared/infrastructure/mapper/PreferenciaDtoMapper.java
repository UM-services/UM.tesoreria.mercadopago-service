package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ReservaVacante;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ChequeraCuotaDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ReservaVacanteDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;

@Component
@RequiredArgsConstructor
public class PreferenciaDtoMapper {

    private final ContextoMercadoPagoDtoMapper contextoMapper;
    private final ChequeraCuotaDtoMapper cuotaMapper;
    private final ReservaVacanteDtoMapper reservaMapper;

    public ContextoConCuota toDomain(UMPreferenceMPDto dto) {
        if (dto == null) return null;
        return ContextoConCuota.builder()
                .contexto(contextoMapper.toDomain(dto.getMercadoPagoContext()))
                .cuota(cuotaMapper.toDomain(dto.getChequeraCuota()))
                .build();
    }

    public ReservaVacante toReserva(UMPreferenceMPDto dto) {
        return dto == null ? null : reservaMapper.toDomain(dto.getReservaVacante());
    }

    public UMPreferenceMPDto toDto(ContextoConCuota contextoConCuota) {
        if (contextoConCuota == null) return null;
        return UMPreferenceMPDto.builder()
                .mercadoPagoContext(contextoMapper.toDto(contextoConCuota.getContexto()))
                .chequeraCuota((ChequeraCuotaDto) contextoConCuota.getCuota().getOriginal())
                .build();
    }

    public UMPreferenceMPDto toVacanteDto(ContextoMercadoPago contexto, ReservaVacante reserva) {
        return UMPreferenceMPDto.builder()
                .mercadoPagoContext(contextoMapper.toDto(contexto))
                .reservaVacante((ReservaVacanteDto) reserva.getOriginal())
                .build();
    }

}
