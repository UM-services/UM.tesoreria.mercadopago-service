package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.support.Jsonifyable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMPreferenceMPDto implements Jsonifyable {

    private MercadoPagoContextDto mercadoPagoContext;
    private ChequeraCuotaDto chequeraCuota;
    private ReservaVacanteDto reservaVacante;

    /* Solo en el endpoint de renovaciones: vencimiento/importe que debe publicarse. */
    private OffsetDateTime fechaVencimientoDestino;
    private BigDecimal importeDestino;

}
