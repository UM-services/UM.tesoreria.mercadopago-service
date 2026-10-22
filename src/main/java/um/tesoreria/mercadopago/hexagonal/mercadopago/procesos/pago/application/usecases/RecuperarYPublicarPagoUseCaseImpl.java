package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service.MercadoPagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.application.service.MensajeriaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ReferenciaExterna;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.OrigenNotificacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecuperarYPublicarPagoUseCaseImpl implements RecuperarYPublicarPagoUseCase {

    private final MercadoPagoService mercadoPagoService;
    private final MensajeriaService mensajeriaService;

    @Override
    public boolean recuperarYPublicar(String dataId, OrigenNotificacion origen) {
        log.debug("\n\nProcessing RecuperarYPublicarPagoUseCaseImpl.recuperarYPublicar for {} (origen {})\n\n",
                dataId, origen);

        long paymentId = Long.parseLong(dataId);
        var pago = mercadoPagoService.recuperarPago(paymentId).orElse(null);
        if (pago == null) {
            log.error("No se pudo recuperar el pago {} desde Mercado Pago", dataId);
            return false;
        }

        log.info("\nExternalReference: {}", pago.getExternalReference());
        ReferenciaExterna referencia = ReferenciaExterna.parse(pago.getExternalReference());
        if (referencia == null) {
            log.error("Formato inválido de externalReference: {}", pago.getExternalReference());
            return false;
        }

        log.debug("\n\nPublish Payment Event\n\n");
        mensajeriaService.publicar(PagoProcesadoEvento.builder()
                .mercadoPagoContextId(referencia.mercadoPagoContextId())
                .chequeraCuotaId(referencia.chequeraCuotaId())
                .reservaVacanteId(referencia.reservaVacanteId())
                .paymentId(dataId)
                .status(pago.getStatus())
                .statusDetail(pago.getStatusDetail())
                .dateApproved(pago.getDateApproved())
                .dateCreated(pago.getDateCreated())
                .dateLastUpdated(pago.getDateLastUpdated())
                .transactionAmount(pago.getTransactionAmount())
                .paymentJson(pago.getJson())
                .source(origen.name())
                .build());
        return true;
    }

}
