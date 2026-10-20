package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.ProcesarWebhookPagoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final ProcesarWebhookPagoUseCase procesarWebhookPagoUseCase;
    private final RecuperarYPublicarPagoUseCase recuperarYPublicarPagoUseCase;

    public String processPaymentWebhook(CabecerasWebhook cabeceras, String dataId) {
        return procesarWebhookPagoUseCase.procesar(cabeceras, dataId);
    }

    public void retrieveAndPublishPayment(String dataId) {
        recuperarYPublicarPagoUseCase.recuperarYPublicar(dataId);
    }

}
