package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.OrigenNotificacion;

public interface RecuperarYPublicarPagoUseCase {

    /**
     * Recupera el pago desde Mercado Pago y publica el evento de pago procesado.
     *
     * @param dataId id del pago en Mercado Pago.
     * @param origen camino por el que llegó la notificación; viaja en el evento como {@code source}.
     * @return {@code true} si el pago se recuperó y el evento se publicó; {@code false} si no se
     * pudo recuperar el pago o su referencia externa es inválida.
     */
    boolean recuperarYPublicar(String dataId, OrigenNotificacion origen);

}
