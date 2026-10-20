package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ReservaVacante;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ReservaVacanteDto;

@Component
public class ReservaVacanteDtoMapper {

    public ReservaVacante toDomain(ReservaVacanteDto dto) {
        if (dto == null) return null;

        var campanha = dto.getCampanha();
        var persona = dto.getPersona();
        var domicilio = dto.getDomicilio();

        return ReservaVacante.builder()
                .reservaVacanteId(dto.getReservaVacanteId())
                .importe(dto.getImporte())
                .vencimiento(dto.getVencimiento())
                .campanhaNombre(campanha == null ? null : campanha.getNombre())
                .campanhaValorReserva(campanha == null ? null : campanha.getValorReserva())
                .personaNombre(persona == null ? null : persona.getNombre())
                .personaApellido(persona == null ? null : persona.getApellido())
                .emailPersonal(domicilio == null ? null : domicilio.getEmailPersonal())
                .original(dto)
                .build();
    }

}
