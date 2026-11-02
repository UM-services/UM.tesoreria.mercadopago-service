package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.application.service;

import java.time.OffsetDateTime;

public final class FechasMercadoPago {

    private static final int HORAS_DE_GRACIA = 30;

    private FechasMercadoPago() {
    }

    public static OffsetDateTime sumarHorasDeGracia(OffsetDateTime fecha) {
        return fecha == null ? null : fecha.plusHours(HORAS_DE_GRACIA);
    }

}
