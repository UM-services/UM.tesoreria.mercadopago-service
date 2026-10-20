package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in;

public interface RecuperarYPublicarPagoUseCase {

    void recuperarYPublicar(String dataId);

}
