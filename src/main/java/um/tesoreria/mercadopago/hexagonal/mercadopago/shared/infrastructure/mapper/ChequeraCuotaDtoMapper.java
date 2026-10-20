package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ChequeraCuotaDto;

@Component
public class ChequeraCuotaDtoMapper {

    public ChequeraCuota toDomain(ChequeraCuotaDto dto) {
        if (dto == null) return null;

        var producto = dto.getProducto();
        var serie = dto.getChequeraSerie();
        var persona = serie == null ? null : serie.getPersona();
        var domicilio = serie == null ? null : serie.getDomicilio();

        var builder = ChequeraCuota.builder()
                .chequeraCuotaId(dto.getChequeraCuotaId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .productoId(dto.getProductoId())
                .alternativaId(dto.getAlternativaId())
                .cuotaId(dto.getCuotaId())
                .productoNombre(producto == null ? null : producto.getNombre())
                .personaNombre(persona == null ? null : persona.getNombre())
                .personaApellido(persona == null ? null : persona.getApellido())
                .emailPersonal(domicilio == null ? null : domicilio.getEmailPersonal())
                .original(dto);

        if (dto.getMes() != null) builder.mes(dto.getMes());
        if (dto.getAnho() != null) builder.anho(dto.getAnho());

        return builder.build();
    }

}
