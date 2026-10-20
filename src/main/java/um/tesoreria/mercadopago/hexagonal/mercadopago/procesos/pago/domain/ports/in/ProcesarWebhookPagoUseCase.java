package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;

public interface ProcesarWebhookPagoUseCase {

    String procesar(CabecerasWebhook cabeceras, String dataId);

}
