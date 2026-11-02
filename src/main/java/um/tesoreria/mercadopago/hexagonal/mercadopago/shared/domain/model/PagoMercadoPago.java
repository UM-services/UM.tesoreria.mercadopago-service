package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PagoMercadoPago {

    private Long paymentId;
    private String externalReference;
    private String status;
    private String statusDetail;
    private OffsetDateTime dateApproved;
    private OffsetDateTime dateCreated;
    /**
     * {@code date_last_updated} del pago en Mercado Pago: ordena los eventos de un mismo pago
     * (un refund es posterior al approved) y permite descartar entregas fuera de orden.
     */
    private OffsetDateTime dateLastUpdated;
    private BigDecimal transactionAmount;
    private String json;

}
