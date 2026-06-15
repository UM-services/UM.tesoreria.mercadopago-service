package um.tesoreria.mercadopago.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableFeignClients(basePackages = "um.tesoreria.mercadopago.client")
@EnableScheduling
public class MercadoPagoConfiguration {
}
