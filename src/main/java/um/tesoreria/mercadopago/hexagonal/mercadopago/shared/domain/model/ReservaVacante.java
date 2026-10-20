package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReservaVacante {

    private UUID reservaVacanteId;
    private BigDecimal importe;
    private OffsetDateTime vencimiento;
    private String campanhaNombre;
    private BigDecimal campanhaValorReserva;
    private String personaNombre;
    private String personaApellido;
    private String emailPersonal;

    /* DTO original recibido, para devolverlo sin perder campos. */
    private Object original;

}
