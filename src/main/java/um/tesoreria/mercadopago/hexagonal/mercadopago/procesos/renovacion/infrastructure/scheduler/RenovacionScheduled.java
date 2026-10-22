package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.ports.in.RenovarVencimientosUseCase;

/**
 * Proceso nocturno: 00:30 (America/Argentina/Mendoza) revisa los enlaces de pago cuyo
 * vencimiento cayo en las ultimas 72 h y publica el vencimiento siguiente si la cuota lo tiene.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.renovacion.enabled", havingValue = "true", matchIfMissing = true)
public class RenovacionScheduled {

    private final RenovarVencimientosUseCase renovarVencimientosUseCase;

    @Value("${app.renovacion.lookback-dias:3}")
    private int lookbackDias;

    @Scheduled(cron = "${app.renovacion.cron:0 30 0 * * *}",
            zone = "${app.renovacion.zone:America/Argentina/Mendoza}")
    public void renovarVencimientos() {
        log.info("Iniciando renovacion de enlaces de pago vencidos (lookback {} dias)", lookbackDias);
        try {
            renovarVencimientosUseCase.ejecutar(lookbackDias, false);
        } catch (Exception e) {
            log.error("La renovacion de vencimientos fallo", e);
        }
    }

}
