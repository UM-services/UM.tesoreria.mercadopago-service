package um.tesoreria.mercadopago.audit;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Snapshot auditable de una preferencia de pago.
 *
 * <p>Se guardan sólo campos de negocio (ids, importe, vencimiento, init point, estado). Quedan
 * afuera a propósito el JSON crudo de Mercado Pago ({@code preference}) y el pago completo: son
 * grandes y pueden contener datos del pagador.</p>
 */
public final class PreferenceSnapshot {

    private static final List<String> FIELDS = List.of(
            "mercadoPagoContextId",
            "preferenceId",
            "chequeraCuotaId",
            "reservaVacanteId",
            "importe",
            "fechaVencimiento",
            "initPoint",
            "changed",
            "activo",
            "lastVencimientoUpdated");

    private PreferenceSnapshot() {
    }

    public static Map<String, Object> of(ContextoMercadoPago contexto) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        if (contexto == null) {
            return snapshot;
        }
        snapshot.put("mercadoPagoContextId", contexto.getMercadoPagoContextId());
        snapshot.put("preferenceId", contexto.getPreferenceId());
        snapshot.put("chequeraCuotaId", contexto.getChequeraCuotaId());
        snapshot.put("reservaVacanteId", contexto.getReservaVacanteId());
        snapshot.put("importe", contexto.getImporte());
        snapshot.put("fechaVencimiento", contexto.getFechaVencimiento());
        snapshot.put("initPoint", contexto.getInitPoint());
        snapshot.put("changed", contexto.getChanged());
        snapshot.put("activo", contexto.getActivo());
        snapshot.put("lastVencimientoUpdated", contexto.getLastVencimientoUpdated());
        return snapshot;
    }

    /** Campos cuyo valor difiere entre dos snapshots. */
    public static List<String> diff(Map<String, Object> before, Map<String, Object> after) {
        List<String> changed = new ArrayList<>();
        for (String field : FIELDS) {
            if (!Objects.equals(before.get(field), after.get(field))) {
                changed.add(field);
            }
        }
        return changed;
    }
}
