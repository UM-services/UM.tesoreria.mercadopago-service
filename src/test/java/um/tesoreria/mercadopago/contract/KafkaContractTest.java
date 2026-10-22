package um.tesoreria.mercadopago.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.util.ReflectionTestUtils;
import um.tesoreria.mercadopago.configuration.KafkaConfiguration;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.adapter.PaymentEventProducerAdapter;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.event.PaymentProcessedEvent;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.mapper.PagoProcesadoMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaContractTest {

    @Test
    void elSerializerNoEscribeTypeIdYConservaElPayload() throws Exception {
        var configuration = new KafkaConfiguration();
        var event = wireEvent();

        Headers headers = new RecordHeaders();
        byte[] payload = configuration.paymentSerializer().serialize("payment-processed", headers, event);

        JsonNode node = new ObjectMapper().readTree(payload);
        assertThat(node.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "mercadoPagoContextId", "chequeraCuotaId", "reservaVacanteId", "paymentId", "status",
                "statusDetail", "dateApproved", "dateCreated", "dateLastUpdated", "transactionAmount",
                "paymentJson", "source");
        assertThat(node.get("paymentId").asText()).isEqualTo("12345");
        assertThat(node.get("transactionAmount").decimalValue()).isEqualByComparingTo("1234.50");
        assertThat(node.get("source").asText()).isEqualTo("WEBHOOK_V1");
        assertThat(node.get("dateLastUpdated").asText()).isEqualTo("2026-10-31T01:00:00Z");
        assertThat(headers.lastHeader("__TypeId__")).isNull();
    }

    @Test
    void elPayloadSeDeserializaConTipoTargetYHeadersVacios() {
        var configuration = new KafkaConfiguration();
        byte[] payload = configuration.paymentSerializer().serialize("payment-processed", new RecordHeaders(), wireEvent());

        var mapper = JsonMapper.builder().findAndAddModules().build();
        var deserializer = new JacksonJsonDeserializer<>(PaymentProcessedEvent.class, mapper, false);

        PaymentProcessedEvent event = deserializer.deserialize("payment-processed", new RecordHeaders(), payload);

        assertThat(event.getPaymentId()).isEqualTo("12345");
        assertThat(event.getStatus()).isEqualTo("approved");
        assertThat(event.getTransactionAmount()).isEqualByComparingTo("1234.50");
        assertThat(event.getDateApproved())
                .isEqualTo(OffsetDateTime.of(2026, 10, 31, 0, 0, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void elPayloadSeDeserializaAunqueVengaUnTypeIdAjeno() {
        var configuration = new KafkaConfiguration();
        byte[] payload = configuration.paymentSerializer().serialize("payment-processed", new RecordHeaders(), wireEvent());
        Headers headers = new RecordHeaders();
        headers.add("__TypeId__", "com.desconocido.Evento".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        var mapper = JsonMapper.builder().findAndAddModules().build();
        var deserializer = new JacksonJsonDeserializer<>(PaymentProcessedEvent.class, mapper, false);

        PaymentProcessedEvent event = deserializer.deserialize("payment-processed", headers, payload);

        assertThat(event.getPaymentId()).isEqualTo("12345");
    }

    @SuppressWarnings("unchecked")
    @Test
    void elAdapterPublicaEnElMismoTopicConLaMismaClave() {
        KafkaTemplate<String, PaymentProcessedEvent> template = mock(KafkaTemplate.class);
        var adapter = new PaymentEventProducerAdapter(template, new PagoProcesadoMapper());

        adapter.publicar(evento());

        verify(template).send(eq("payment-processed"), eq("12345"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void laConfiguracionConservaAcksYReintentos() {
        var configuration = new KafkaConfiguration();
        ReflectionTestUtils.setField(configuration, "bootstrapServers", "kafka:9092");

        var factory = (DefaultKafkaProducerFactory<?, ?>) configuration.paymentProducerFactory();

        assertThat(factory.getConfigurationProperties())
                .containsEntry(ProducerConfig.ACKS_CONFIG, "all")
                .containsEntry(ProducerConfig.RETRIES_CONFIG, 3)
                .containsEntry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        org.springframework.kafka.support.serializer.JacksonJsonSerializer.class);
    }

    private PagoProcesadoEvento evento() {
        return PagoProcesadoEvento.builder()
                .mercadoPagoContextId(5L)
                .chequeraCuotaId(7L)
                .paymentId("12345")
                .status("approved")
                .statusDetail("accredited")
                .dateApproved(OffsetDateTime.of(2026, 10, 31, 0, 0, 0, 0, ZoneOffset.UTC))
                .dateCreated(OffsetDateTime.of(2026, 10, 30, 0, 0, 0, 0, ZoneOffset.UTC))
                .dateLastUpdated(OffsetDateTime.of(2026, 10, 31, 1, 0, 0, 0, ZoneOffset.UTC))
                .transactionAmount(new BigDecimal("1234.50"))
                .paymentJson("{\"id\":12345}")
                .source("WEBHOOK_V1")
                .build();
    }

    private PaymentProcessedEvent wireEvent() {
        return new PagoProcesadoMapper().toWire(evento());
    }

}
