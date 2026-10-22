package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.exception;

public class MercadoPagoException extends RuntimeException {

    private final Integer statusCode;
    private final String apiResponse;

    public MercadoPagoException(String message, Integer statusCode, String apiResponse, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.apiResponse = apiResponse;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getApiResponse() {
        return apiResponse;
    }

}
