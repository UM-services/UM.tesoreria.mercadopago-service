package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;

@Component
public class ContextoMercadoPagoDtoMapper {

    public ContextoMercadoPago toDomain(MercadoPagoContextDto dto) {
        if (dto == null) return null;

        var builder = ContextoMercadoPago.builder()
                .mercadoPagoContextId(dto.getMercadoPagoContextId())
                .chequeraCuotaId(dto.getChequeraCuotaId())
                .reservaVacanteId(dto.getReservaVacanteId())
                .initPoint(dto.getInitPoint())
                .fechaVencimiento(dto.getFechaVencimiento())
                .lastVencimientoUpdated(dto.getLastVencimientoUpdated())
                .preferenceId(dto.getPreferenceId())
                .preference(dto.getPreference())
                .chequeraPagoId(dto.getChequeraPagoId())
                .idMercadoPago(dto.getIdMercadoPago())
                .status(dto.getStatus())
                .fechaPago(dto.getFechaPago())
                .fechaAcreditacion(dto.getFechaAcreditacion())
                .dateLastUpdated(dto.getDateLastUpdated())
                .payment(dto.getPayment())
                .chequeraCuotaOriginal(dto.getChequeraCuota())
                .reservaVacanteOriginal(dto.getReservaVacante());

        if (dto.getImporte() != null) builder.importe(dto.getImporte());
        if (dto.getChanged() != null) builder.changed(dto.getChanged());
        if (dto.getActivo() != null) builder.activo(dto.getActivo());
        if (dto.getImportePagado() != null) builder.importePagado(dto.getImportePagado());

        return builder.build();
    }

    public MercadoPagoContextDto toDto(ContextoMercadoPago domain) {
        if (domain == null) return null;

        var builder = MercadoPagoContextDto.builder()
                .mercadoPagoContextId(domain.getMercadoPagoContextId())
                .chequeraCuotaId(domain.getChequeraCuotaId())
                .reservaVacanteId(domain.getReservaVacanteId())
                .initPoint(domain.getInitPoint())
                .fechaVencimiento(domain.getFechaVencimiento())
                .lastVencimientoUpdated(domain.getLastVencimientoUpdated())
                .preferenceId(domain.getPreferenceId())
                .preference(domain.getPreference())
                .chequeraPagoId(domain.getChequeraPagoId())
                .idMercadoPago(domain.getIdMercadoPago())
                .status(domain.getStatus())
                .fechaPago(domain.getFechaPago())
                .fechaAcreditacion(domain.getFechaAcreditacion())
                .dateLastUpdated(domain.getDateLastUpdated())
                .payment(domain.getPayment())
                .chequeraCuota((um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ChequeraCuotaDto) domain.getChequeraCuotaOriginal())
                .reservaVacante((um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ReservaVacanteDto) domain.getReservaVacanteOriginal());

        if (domain.getImporte() != null) builder.importe(domain.getImporte());
        if (domain.getChanged() != null) builder.changed(domain.getChanged());
        if (domain.getActivo() != null) builder.activo(domain.getActivo());
        if (domain.getImportePagado() != null) builder.importePagado(domain.getImportePagado());

        return builder.build();
    }

}
