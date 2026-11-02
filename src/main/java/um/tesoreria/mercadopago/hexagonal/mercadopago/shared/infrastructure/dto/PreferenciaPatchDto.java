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

/**
 * Cambios parciales sobre la preferencia de un contexto. Los campos nulos no se envian.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaPatchDto implements Jsonifyable {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fechaVencimiento;

    private BigDecimal importe;
    private String initPoint;
    private String preferenceId;
    private String preference;
    private Byte changed;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime lastVencimientoUpdated;

}
