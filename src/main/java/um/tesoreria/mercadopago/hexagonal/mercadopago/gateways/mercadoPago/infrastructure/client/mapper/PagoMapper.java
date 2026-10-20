package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.infrastructure.client.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.resources.payment.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagoMercadoPago;

@Component
@Slf4j
public class PagoMapper {

    public PagoMercadoPago toDomain(Payment payment) {
        if (payment == null) return null;

        String paymentString = null;
        try {
            paymentString = JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            log.error("PaymentString Error -> {}", e.getMessage());
        }

        return PagoMercadoPago.builder()
                .paymentId(payment.getId())
                .externalReference(payment.getExternalReference())
                .status(payment.getStatus())
                .statusDetail(payment.getStatusDetail())
                .dateApproved(payment.getDateApproved())
                .dateCreated(payment.getDateCreated())
                .transactionAmount(payment.getTransactionAmount())
                .json(paymentString)
                .build();
    }

}
