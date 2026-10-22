package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ResultadoWebhookPago;

public interface ProcesarWebhookPagoUseCase {

    /**
     * Procesa una notificación webhook v1: valida la firma y, si es correcta, recupera y publica el pago.
     */
    ResultadoWebhookPago procesar(CabecerasWebhook cabeceras, String dataId);

    /**
     * Procesa una notificación IPN legacy (topic=payment). No trae firma válida, pero el pago se
     * autentica al recuperarlo con las credenciales propias de la aplicación.
     */
    ResultadoWebhookPago procesarNotificacionLegada(String paymentId);

}
