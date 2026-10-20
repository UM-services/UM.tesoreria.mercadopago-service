package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.event.PaymentProcessedEvent;

@Component
public class PagoProcesadoMapper {

    public PaymentProcessedEvent toWire(PagoProcesadoEvento evento) {
        return PaymentProcessedEvent.builder()
                .mercadoPagoContextId(evento.getMercadoPagoContextId())
                .chequeraCuotaId(evento.getChequeraCuotaId())
                .reservaVacanteId(evento.getReservaVacanteId())
                .paymentId(evento.getPaymentId())
                .status(evento.getStatus())
                .statusDetail(evento.getStatusDetail())
                .dateApproved(evento.getDateApproved())
                .dateCreated(evento.getDateCreated())
                .transactionAmount(evento.getTransactionAmount())
                .paymentJson(evento.getPaymentJson())
                .build();
    }

}
