package um.tesoreria.mercadopago;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;
import um.tesoreria.mercadopago.domain.event.PaymentProcessedEvent;

@SpringBootTest
class MercadoPagoApplicationTests {

	@MockitoBean
	private KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

	@Test
	void contextLoads() {
	}

}
