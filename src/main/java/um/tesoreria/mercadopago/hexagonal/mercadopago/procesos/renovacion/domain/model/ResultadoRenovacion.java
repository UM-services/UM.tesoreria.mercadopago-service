package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.model;

import java.util.List;

/**
 * Resumen de una corrida del proceso de renovacion de enlaces vencidos.
 */
public record ResultadoRenovacion(boolean dryRun,
                                  int paginas,
                                  int escaneados,
                                  int actualizados,
                                  int creados,
                                  int sinCambios,
                                  int fallidos,
                                  long duracionMs,
                                  List<Long> fallidosIds) {
}
