package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.model;

/**
 * Parametros de una corrida del proceso de renovacion.
 */
public record SolicitudRenovacion(Integer lookbackDias, Boolean dryRun) {

    public int lookbackEfectivo() {
        return lookbackDias == null ? 3 : Math.max(0, lookbackDias);
    }

    public boolean esDryRun() {
        return Boolean.TRUE.equals(dryRun);
    }
}
