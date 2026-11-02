package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TipoChequeraTarjeta {

    private UUID id;
    private Integer tipoChequeraId;
    private Integer alternativaId;
    private Integer installments;
    private Integer defaultInstallments;
    private Byte active;

}
