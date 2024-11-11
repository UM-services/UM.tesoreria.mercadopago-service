package um.tesoreria.mercadopago.service.configuration;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "um.tesoreria.mercadopago.service.client")
public class MercadoPagoConfiguration {
}
