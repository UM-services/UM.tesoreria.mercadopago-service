package um.tesoreria.mercadopago;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.event.PaymentProcessedEvent;

@SpringBootTest(properties = {
		// En tests no hay broker: sin KafkaAdmin no se espera el timeout de creación de topics.
		"spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration"
})
class MercadoPagoApplicationTests {

	@MockitoBean
	private KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

	@Test
	void contextLoads() {
	}

}
