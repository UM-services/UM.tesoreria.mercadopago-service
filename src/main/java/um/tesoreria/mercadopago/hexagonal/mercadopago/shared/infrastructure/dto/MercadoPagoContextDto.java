package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.support.Jsonifyable;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoContextDto implements Jsonifyable {

    private Long mercadoPagoContextId;
    private Long chequeraCuotaId;
    private UUID reservaVacanteId;
    private String initPoint;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fechaVencimiento;

    @Builder.Default
    private BigDecimal importe = BigDecimal.ZERO;
    @Builder.Default
    private Byte changed = 0;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime lastVencimientoUpdated;

    private String preferenceId;
    private String preference;
    @Builder.Default
    private Byte activo = 0;
    private Long chequeraPagoId;
    private String idMercadoPago;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fechaPago;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fechaAcreditacion;

    /** {@code date_last_updated} del pago en MP (orden de los eventos); se reenvía tal cual a core. */
    private OffsetDateTime dateLastUpdated;

    @Builder.Default
    private BigDecimal importePagado = BigDecimal.ZERO;
    private String payment;

    private ChequeraCuotaDto chequeraCuota;
    private ReservaVacanteDto reservaVacante;

}
