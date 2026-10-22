package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model;

public record ResultadoWebhookPago(EstadoWebhookPago estado, String mensaje) {

    public boolean exitoso() {
        return estado == EstadoWebhookPago.PROCESADO;
    }

}
