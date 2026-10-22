package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Contexto activo cuyo vencimiento publicado ya no es el vigente y la cuota tiene un
 * vencimiento posterior para publicar.
 */
public record CandidatoRenovacion(ContextoMercadoPago contexto,
                                  ChequeraCuota cuota,
                                  OffsetDateTime fechaVencimientoDestino,
                                  BigDecimal importeDestino) {

    public boolean requiereActualizacion() {
        if (contexto == null || fechaVencimientoDestino == null || importeDestino == null) {
            return false;
        }
        boolean mismaFecha = contexto.getFechaVencimiento() != null
                && fechaVencimientoDestino.isEqual(contexto.getFechaVencimiento());
        boolean mismoImporte = contexto.getImporte() != null
                && importeDestino.compareTo(contexto.getImporte()) == 0;
        return !mismaFecha || !mismoImporte;
    }
}
