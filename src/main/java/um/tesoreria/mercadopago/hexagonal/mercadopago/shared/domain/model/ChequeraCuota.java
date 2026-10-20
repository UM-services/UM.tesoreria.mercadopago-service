package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChequeraCuota {

    private Long chequeraCuotaId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;

    @Builder.Default
    private Integer mes = 0;

    @Builder.Default
    private Integer anho = 0;

    private String productoNombre;
    private String personaNombre;
    private String personaApellido;
    private String emailPersonal;

    /* DTO original de core, para devolverlo sin perder campos. */
    private Object original;

}
