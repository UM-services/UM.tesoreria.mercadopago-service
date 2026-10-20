package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.TipoChequeraMercadoPagoCreditCardDto;

@Component
public class TipoChequeraTarjetaDtoMapper {

    public TipoChequeraTarjeta toDomain(TipoChequeraMercadoPagoCreditCardDto dto) {
        if (dto == null) return null;

        return TipoChequeraTarjeta.builder()
                .id(dto.getId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .alternativaId(dto.getAlternativaId())
                .installments(dto.getInstallments())
                .defaultInstallments(dto.getDefaultInstallments())
                .active(dto.getActive())
                .build();
    }

}
