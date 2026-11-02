package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.audit.AuditEventPublisher;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.service.CheckingService;

/**
 * Scheduler legado del barrido de 90 dias. Deshabilitado por defecto: lo reemplaza
 * {@code RenovacionScheduled}. Se puede reactivar con
 * {@code app.checking.legacy-scheduler.enabled=true}.
 *
 * @deprecated Scheduler legado (04:00 UTC, ventana de 90 días). Lo reemplaza
 * {@code RenovacionScheduled} (00:30 ART, ventana de 72 h).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.checking.legacy-scheduler.enabled", havingValue = "true")
@Deprecated
public class CheckingScheduled {

    private final CheckingService checkingService;
    private final AuditEventPublisher audit;

    @Scheduled(cron = "0 0 4 * * *")
    public void checkingAllActive() {
        long started = System.currentTimeMillis();
        try {
            checkingService.checkingAllActive();
            audit.publishScheduled("checking-all-active", "OK",
                    System.currentTimeMillis() - started, null);
        } catch (RuntimeException e) {
            audit.publishScheduled("checking-all-active", "ERROR",
                    System.currentTimeMillis() - started, e);
            throw e;
        }
    }

}
