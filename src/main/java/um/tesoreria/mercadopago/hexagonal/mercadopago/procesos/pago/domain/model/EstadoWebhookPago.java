package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model;

/**
 * Resultado posible del procesamiento de una notificación de pago de Mercado Pago.
 * El adaptador web traduce cada estado al código HTTP correspondiente.
 */
public enum EstadoWebhookPago {

    PROCESADO,
    DATA_ID_FALTANTE,
    DATA_ID_INVALIDO,
    FIRMA_FALTANTE,
    REQUEST_ID_FALTANTE,
    FIRMA_MAL_FORMADA,
    FIRMA_INVALIDA,
    PAGO_NO_ENCONTRADO,
    /** Notificación IPN legacy recibida con el fallback apagado: se acknowledgea y se ignora. */
    LEGACY_IGNORADA

}
