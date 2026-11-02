package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model;

public record CabecerasWebhook(String xSignature, String xRequestId) {
}
