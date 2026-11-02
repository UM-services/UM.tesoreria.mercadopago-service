package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.model.ResultadoRenovacion;

public interface RenovarVencimientosUseCase {

    /**
     * Recorre los contextos activos cuyo vencimiento publicado cayo en la ventana y, si la cuota
     * tiene un vencimiento posterior, actualiza el enlace de Mercado Pago.
     *
     * @param lookbackDias dias hacia atras sobre el vencimiento publicado (72 h = 3 dias)
     * @param dryRun       si es true no llama a Mercado Pago ni escribe en el core
     */
    ResultadoRenovacion ejecutar(int lookbackDias, boolean dryRun);

}
