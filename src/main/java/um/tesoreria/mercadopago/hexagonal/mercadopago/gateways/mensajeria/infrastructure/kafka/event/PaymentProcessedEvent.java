package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private Long mercadoPagoContextId;
    private Long chequeraCuotaId;
    private UUID reservaVacanteId;
    private String paymentId;
    private String status;
    private String statusDetail;
    private OffsetDateTime dateApproved;
    private OffsetDateTime dateCreated;
    private BigDecimal transactionAmount;
    private String paymentJson;
}
