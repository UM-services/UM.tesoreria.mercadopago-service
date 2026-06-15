package um.tesoreria.mercadopago.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import um.tesoreria.mercadopago.util.Jsonifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private Long mercadoPagoContextId;
    private Long chequeraCuotaId;
    private String paymentId;
    private String status;
    private String statusDetail;
    private OffsetDateTime dateApproved;
    private OffsetDateTime dateCreated;
    private BigDecimal transactionAmount;
    private String paymentJson;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }
}
