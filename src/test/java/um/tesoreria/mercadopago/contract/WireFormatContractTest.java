package um.tesoreria.mercadopago.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.PaymentNotificationDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.infrastructure.kafka.event.PaymentProcessedEvent;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.CampanhaDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ReservaVacanteDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WireFormatContractTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules().build();
    private static final OffsetDateTime FECHA = OffsetDateTime.of(2026, 10, 31, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void elContextoConservaLosMismosCamposYFormatoDeFechas() throws Exception {
        var contexto = MercadoPagoContextDto.builder()
                .mercadoPagoContextId(5L)
                .chequeraCuotaId(7L)
                .reservaVacanteId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .initPoint("https://mp/init")
                .fechaVencimiento(FECHA)
                .importe(new BigDecimal("1234.50"))
                .changed((byte) 1)
                .lastVencimientoUpdated(FECHA)
                .preferenceId("pref-1")
                .preference("{\"id\":\"pref-1\"}")
                .activo((byte) 1)
                .chequeraPagoId(9L)
                .idMercadoPago("mp-1")
                .status("approved")
                .fechaPago(FECHA)
                .fechaAcreditacion(FECHA)
                .dateLastUpdated(FECHA.plusHours(2))
                .importePagado(new BigDecimal("1234.50"))
                .payment("{\"id\":1}")
                .build();

        JsonNode node = MAPPER.readTree(MAPPER.writeValueAsString(contexto));

        assertThat(node.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "mercadoPagoContextId", "chequeraCuotaId", "reservaVacanteId", "initPoint",
                "fechaVencimiento", "importe", "changed", "lastVencimientoUpdated", "preferenceId",
                "preference", "activo", "chequeraPagoId", "idMercadoPago", "status", "fechaPago",
                "fechaAcreditacion", "dateLastUpdated", "importePagado", "payment", "chequeraCuota",
                "reservaVacante");
        assertThat(node.get("fechaVencimiento").asText()).isEqualTo("2026-10-31T00:00:00Z");
        assertThat(node.get("reservaVacanteId").asText()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(node.get("importe").decimalValue()).isEqualByComparingTo("1234.50");
        assertThat(node.get("changed").asInt()).isEqualTo(1);
        assertThat(node.get("chequeraCuota").isNull()).isTrue();
        assertThat(node.get("reservaVacante").isNull()).isTrue();
    }

    @Test
    void elContextoPorDefectoMantieneLosValoresQueViajanACore() throws Exception {
        JsonNode node = MAPPER.readTree(MAPPER.writeValueAsString(MercadoPagoContextDto.builder().build()));

        assertThat(node.get("importe").decimalValue()).isEqualByComparingTo("0");
        assertThat(node.get("changed").asInt()).isZero();
        assertThat(node.get("activo").asInt()).isZero();
        assertThat(node.get("importePagado").decimalValue()).isEqualByComparingTo("0");
    }

    @Test
    void laPreferenciaConservaElEnvelopeDeCuotaYVacante() throws Exception {
        var campanha = CampanhaDto.builder()
                .campanhaId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .nombre("Curso de verano")
                .build();
        var reserva = ReservaVacanteDto.builder()
                .reservaVacanteId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .campanha(campanha)
                .build();
        var dto = UMPreferenceMPDto.builder()
                .mercadoPagoContext(MercadoPagoContextDto.builder().mercadoPagoContextId(1L).build())
                .reservaVacante(reserva)
                .build();

        JsonNode node = MAPPER.readTree(MAPPER.writeValueAsString(dto));

        assertThat(node.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "mercadoPagoContext", "chequeraCuota", "reservaVacante",
                "fechaVencimientoDestino", "importeDestino");
        assertThat(node.get("chequeraCuota").isNull()).isTrue();
        assertThat(node.get("fechaVencimientoDestino").isNull()).isTrue();
        assertThat(node.get("importeDestino").isNull()).isTrue();
        assertThat(node.get("reservaVacante").get("campanha").get("nombre").asText())
                .isEqualTo("Curso de verano");
    }

    @Test
    void laNotificacionDePagoDeserializaLosNombresSnakeCase() throws Exception {
        var notification = MAPPER.readValue("""
                {"action":"payment.created","api_version":"v1","data":{"id":"12345"},
                 "date_created":"2026-10-20T12:30:00Z","id":99,"live_mode":true,"type":"payment",
                 "user_id":"42"}
                """, PaymentNotificationDto.class);

        assertThat(notification.getApiVersion()).isEqualTo("v1");
        assertThat(notification.getData().getId()).isEqualTo("12345");
        assertThat(notification.getDateCreated()).isEqualTo(OffsetDateTime.of(2026, 10, 20, 12, 30, 0, 0, ZoneOffset.UTC));
        assertThat(notification.getLiveMode()).isTrue();
        assertThat(notification.getUserId()).isEqualTo("42");
        assertThat(notification.getId()).isEqualTo(99L);
    }

    @Test
    void elEventoDePagoConservaLosMismosCamposYFormatoDeFecha() throws Exception {
        var event = PaymentProcessedEvent.builder()
                .mercadoPagoContextId(5L)
                .chequeraCuotaId(7L)
                .reservaVacanteId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .paymentId("12345")
                .status("approved")
                .statusDetail("accredited")
                .dateApproved(FECHA)
                .dateCreated(FECHA)
                .dateLastUpdated(FECHA.plusHours(1))
                .transactionAmount(new BigDecimal("1234.50"))
                .paymentJson("{\"id\":12345}")
                .source("IPN_LEGACY")
                .build();

        JsonNode node = MAPPER.readTree(MAPPER.writeValueAsString(event));

        assertThat(node.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "mercadoPagoContextId", "chequeraCuotaId", "reservaVacanteId", "paymentId", "status",
                "statusDetail", "dateApproved", "dateCreated", "dateLastUpdated", "transactionAmount",
                "paymentJson", "source");
        assertThat(node.has("dateApproved")).isTrue();
        // El formato real de las fechas en el wire (ISO-8601) lo verifica KafkaContractTest con el
        // serializer de producción: este mapper de tools.jackson escribe epochs por defecto.
        assertThat(node.has("dateLastUpdated")).isTrue();
        assertThat(node.get("transactionAmount").decimalValue()).isEqualByComparingTo("1234.50");
        assertThat(node.get("source").asText()).isEqualTo("IPN_LEGACY");
    }

}
