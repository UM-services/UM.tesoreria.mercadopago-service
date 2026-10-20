package um.tesoreria.mercadopago.contract;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.infrastructure.scheduler.CheckingScheduled;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.infrastructure.web.controller.CheckingController;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingContractTest {

    @Test
    void mantieneElCronDeLasCuatroEnElScheduler() {
        assertThat(cron(CheckingScheduled.class, "checkingAllActive")).isEqualTo("0 0 4 * * *");
    }

    @Test
    void elControllerYaNoEjecutaElJob() {
        assertThat(cron(CheckingController.class, "checkingAllActive")).isNull();
    }

    private String cron(Class<?> type, String methodName) {
        try {
            Method method = type.getDeclaredMethod(methodName);
            Scheduled scheduled = method.getAnnotation(Scheduled.class);
            return scheduled == null ? null : scheduled.cron();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
