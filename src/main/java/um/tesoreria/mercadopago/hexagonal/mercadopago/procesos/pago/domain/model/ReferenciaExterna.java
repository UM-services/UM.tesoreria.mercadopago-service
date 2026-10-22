package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model;

import java.util.UUID;

public record ReferenciaExterna(Long chequeraCuotaId, Long mercadoPagoContextId, UUID reservaVacanteId) {

    private static final String SEPARADOR = "-";
    private static final int PARTES_REFERENCIA_CORTA = 3;

    public static ReferenciaExterna parse(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return null;
        }
        String[] parts = externalReference.split(SEPARADOR);

        if (parts.length == 6) {
            try {
                String uuidString = String.join(SEPARADOR, parts[0], parts[1], parts[2], parts[3], parts[4]);
                UUID reservaVacanteId = UUID.fromString(uuidString);
                Long mercadoPagoContextId = Long.parseLong(parts[5]);
                return new ReferenciaExterna(null, mercadoPagoContextId, reservaVacanteId);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        if (parts.length >= 2 && parts.length <= PARTES_REFERENCIA_CORTA) {
            try {
                int chequeraCuotaIndex = parts.length == PARTES_REFERENCIA_CORTA ? 1 : 0;
                int mercadoPagoContextIndex = parts.length == PARTES_REFERENCIA_CORTA ? 2 : 1;
                Long chequeraCuotaId = Long.parseLong(parts[chequeraCuotaIndex]);
                Long mercadoPagoContextId = Long.parseLong(parts[mercadoPagoContextIndex]);
                return new ReferenciaExterna(chequeraCuotaId, mercadoPagoContextId, null);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

}
