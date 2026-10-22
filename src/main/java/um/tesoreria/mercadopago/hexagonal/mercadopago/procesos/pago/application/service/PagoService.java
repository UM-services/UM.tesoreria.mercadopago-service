package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.OrigenNotificacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ResultadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.ProcesarWebhookPagoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final ProcesarWebhookPagoUseCase procesarWebhookPagoUseCase;
    private final RecuperarYPublicarPagoUseCase recuperarYPublicarPagoUseCase;

    public ResultadoWebhookPago processPaymentWebhook(CabecerasWebhook cabeceras, String dataId) {
        return procesarWebhookPagoUseCase.procesar(cabeceras, dataId);
    }

    public ResultadoWebhookPago processLegacyPaymentNotification(String paymentId) {
        return procesarWebhookPagoUseCase.procesarNotificacionLegada(paymentId);
    }

    public boolean retrieveAndPublishPayment(String dataId) {
        return recuperarYPublicarPagoUseCase.recuperarYPublicar(dataId, OrigenNotificacion.MANUAL);
    }

}
