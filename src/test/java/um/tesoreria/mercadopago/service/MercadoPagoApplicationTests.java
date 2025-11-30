package um.tesoreria.mercadopago.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;
import um.tesoreria.mercadopago.service.domain.event.PaymentProcessedEvent;

@SpringBootTest
class MercadoPagoApplicationTests {

	@MockitoBean
	private KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

	@Test
	void contextLoads() {
	}

}
