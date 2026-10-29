package um.tesoreria.mercadopago.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guarda contra la regresión de Spring Boot 4: la auto-configuración de Kafka vive en el módulo
 * {@code spring-boot-kafka}, que llega con {@code spring-boot-starter-kafka}. Sin él no hay bean
 * {@code KafkaAdmin}: el topic de auditoría declarado con 3 particiones se auto-crearía con 1.
 */
class KafkaAutoconfiguracionTest {

    @Test
    void elModuloDeAutoconfiguracionDeKafkaEstaEnElClasspath() {
        var classLoader = getClass().getClassLoader();

        assertThat(ClassUtils.isPresent(
                "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration", classLoader))
                .as("sin spring-boot-kafka no hay KafkaAdmin y los NewTopic no se aplican")
                .isTrue();
        assertThat(ClassUtils.isPresent(
                "org.springframework.boot.kafka.autoconfigure.KafkaProperties", classLoader))
                .isTrue();
    }

}
