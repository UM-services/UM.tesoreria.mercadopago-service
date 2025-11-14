package um.tesoreria.mercadopago.service.scheduled;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.service.service.CheckingService;

@Component
@RequiredArgsConstructor
public class CheckingScheduled {

    private final CheckingService service;

    @Scheduled(cron = "0 0 4 * * *")
    public void checkingAllActive() {
        service.checkingAllActive();
    }

}
