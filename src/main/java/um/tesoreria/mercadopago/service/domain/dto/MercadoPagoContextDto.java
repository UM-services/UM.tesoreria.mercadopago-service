package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoContextDto {
    
    private Long mercadoPagoContextId;
    private Long chequeraCuotaId;
    private String initPoint;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    private OffsetDateTime fechaVencimiento;
    
    private BigDecimal importe = BigDecimal.ZERO;
    private String preference;
    private Byte activo = 0;
    private Long chequeraPagoId;
    private String idMercadoPago;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    private OffsetDateTime fechaPago;

    private BigDecimal importePagado = BigDecimal.ZERO;
    private String payment;

}
