package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.exception;

public class ChequeraCuotaException extends RuntimeException {

    public ChequeraCuotaException(Long chequeraCuotaId) {
        super("No se encontró la cuota con id: " + chequeraCuotaId);
    }

}
