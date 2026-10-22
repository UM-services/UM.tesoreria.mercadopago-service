package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model;

/**
 * Camino por el que se recuperó y publicó un pago. Viaja en el evento como {@code source} para
 * poder distinguir en los consumidores y en los logs si el duplicado vino del webhook v1, del
 * fallback IPN legacy o de un replay manual.
 */
public enum OrigenNotificacion {

    WEBHOOK_V1,
    IPN_LEGACY,
    MANUAL

}
