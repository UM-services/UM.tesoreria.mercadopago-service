package um.tesoreria.mercadopago.service.util;

import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
public class DateToolMP {

    public static OffsetDateTime convertToMPDate(OffsetDateTime date) {
        log.debug("Processing convertToMPDate");
        log.debug("Original Date -> {}", date);

        // Agrega 30 horas al OffsetDateTime original
        var dateMP = date.plusHours(30);
        log.debug("Offset Date -> {}", dateMP);

        return dateMP;
    }

}
