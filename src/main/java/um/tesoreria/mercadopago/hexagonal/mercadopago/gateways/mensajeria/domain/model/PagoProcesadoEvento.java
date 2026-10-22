package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model;

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
public class PagoProcesadoEvento {

    private Long mercadoPagoContextId;
    private Long chequeraCuotaId;
    private UUID reservaVacanteId;
    private String paymentId;
    private String status;
    private String statusDetail;
    private OffsetDateTime dateApproved;
    private OffsetDateTime dateCreated;
    /** {@code date_last_updated} del pago en Mercado Pago (orden de los eventos). */
    private OffsetDateTime dateLastUpdated;
    private BigDecimal transactionAmount;
    private String paymentJson;
    /** {@code WEBHOOK_V1} | {@code IPN_LEGACY} | {@code MANUAL}. */
    private String source;

}
