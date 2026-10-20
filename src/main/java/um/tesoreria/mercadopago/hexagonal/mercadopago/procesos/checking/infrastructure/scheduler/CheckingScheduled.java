package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.service.CheckingService;

@Component
@RequiredArgsConstructor
public class CheckingScheduled {

    private final CheckingService checkingService;

    @Scheduled(cron = "0 0 4 * * *")
    public void checkingAllActive() {
        checkingService.checkingAllActive();
    }

}
