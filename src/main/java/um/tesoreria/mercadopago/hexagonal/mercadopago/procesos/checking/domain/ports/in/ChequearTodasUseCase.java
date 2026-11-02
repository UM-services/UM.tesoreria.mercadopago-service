package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in;

/**
 * @deprecated Barrido legado (90 días). Lo reemplaza {@code RenovarVencimientosUseCase},
 * que corre a las 00:30 con ventana de 72 h, filtra cuotas pagadas y pagina.
 */
@Deprecated
public interface ChequearTodasUseCase {

    String chequearTodas();

}
