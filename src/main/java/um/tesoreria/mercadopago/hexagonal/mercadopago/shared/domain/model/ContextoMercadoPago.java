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
public class ContextoMercadoPago {

    private Long mercadoPagoContextId;
    private Long chequeraCuotaId;
    private UUID reservaVacanteId;
    private String initPoint;
    private OffsetDateTime fechaVencimiento;

    @Builder.Default
    private BigDecimal importe = BigDecimal.ZERO;

    @Builder.Default
    private Byte changed = 0;

    private OffsetDateTime lastVencimientoUpdated;
    private String preferenceId;
    private String preference;

    @Builder.Default
    private Byte activo = 0;

    private Long chequeraPagoId;
    private String idMercadoPago;
    private String status;
    private OffsetDateTime fechaPago;
    private OffsetDateTime fechaAcreditacion;

    @Builder.Default
    private BigDecimal importePagado = BigDecimal.ZERO;

    private String payment;

    /* Campos anidados tal como llegan de core; se reenvian sin transformar. */
    private Object chequeraCuotaOriginal;
    private Object reservaVacanteOriginal;

}
