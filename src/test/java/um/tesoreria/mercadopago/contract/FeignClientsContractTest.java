package um.tesoreria.mercadopago.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import feign.Client;
import feign.Feign;
import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.format.support.DefaultFormattingConversionService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.infrastructure.client.ChequeraCuotaClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client.ContextoCoreClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.infrastructure.client.ContextoQueryClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.infrastructure.client.TipoChequeraMercadoPagoCreditCardClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignClientsContractTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules().build();

    private RecordingClient http;
    private RecordingEncoder encoder;
    private Feign.Builder feign;

    @BeforeEach
    void setUp() {
        http = new RecordingClient();
        encoder = new RecordingEncoder();
        var contract = new SpringMvcContract(Collections.emptyList(), new DefaultFormattingConversionService());
        Decoder decoder = (response, type) -> type == String.class ? "" : null;
        feign = Feign.builder().contract(contract).client(http).encoder(encoder).decoder(decoder);
    }

    @Test
    void declaraLosMismosClientesYServiceIds() {
        assertClient(ChequeraCuotaClient.class, "/api/tesoreria/core/chequeraCuota", "chequeraCuotaClient");
        assertClient(ContextoCoreClient.class, "/api/tesoreria/core/mercadopago", "mercadoPagoCoreClient");
        assertClient(ContextoQueryClient.class, "/api/tesoreria/core/mercadoPagoContext", "mercadoPagoContextClient");
        assertClient(TipoChequeraMercadoPagoCreditCardClient.class,
                "/api/tesoreria/core/tipoChequeraMercadoPagoCreditCard", "tipoChequeraMercadoPagoCreditCardClient");
    }

    @Test
    void chequeraCuotaMantieneSusRutas() {
        target(ChequeraCuotaClient.class).findAllPendientes(1, 2, 3L, 4);
        assertThat(http.last.httpMethod()).isEqualTo(Request.HttpMethod.GET);
        assertThat(http.last.url()).isEqualTo(base(ChequeraCuotaClient.class) + "/chequera/pendientes/1/2/3/4");

        target(ChequeraCuotaClient.class).findByChequeraCuotaId(99L);
        assertThat(http.last.url()).isEqualTo(base(ChequeraCuotaClient.class) + "/99");
    }

    @Test
    void contextoMantieneSusRutas() {
        target(ContextoQueryClient.class).findAllActiveToChange();
        assertThat(http.last.httpMethod()).isEqualTo(Request.HttpMethod.GET);
        assertThat(http.last.url()).isEqualTo(base(ContextoQueryClient.class) + "/all/active/to/change");
    }

    @Test
    void coreMantieneSusRutasYElPayloadDeUpdate() throws Exception {
        target(ContextoCoreClient.class).makeContext(7L);
        assertThat(http.last.httpMethod()).isEqualTo(Request.HttpMethod.GET);
        assertThat(http.last.url()).isEqualTo(base(ContextoCoreClient.class) + "/makeContext/7");

        target(ContextoCoreClient.class).makeContextsChequera(1, 2, 3L, 4);
        assertThat(http.last.url()).isEqualTo(base(ContextoCoreClient.class) + "/makeContextsChequera/1/2/3/4");

        var contexto = MercadoPagoContextDto.builder()
                .mercadoPagoContextId(5L)
                .chequeraCuotaId(7L)
                .importe(new BigDecimal("1234.50"))
                .changed((byte) 0)
                .activo((byte) 1)
                .build();

        target(ContextoCoreClient.class).updateContext(contexto, 5L);
        assertThat(http.last.httpMethod()).isEqualTo(Request.HttpMethod.PUT);
        assertThat(http.last.url()).isEqualTo(base(ContextoCoreClient.class) + "/updateContext/5");
        JSONAssert.assertEquals("""
                {"mercadoPagoContextId":5,"chequeraCuotaId":7,"reservaVacanteId":null,"initPoint":null,
                 "fechaVencimiento":null,"importe":1234.50,"changed":0,"lastVencimientoUpdated":null,
                 "preferenceId":null,"preference":null,"activo":1,"chequeraPagoId":null,
                 "idMercadoPago":null,"status":null,"fechaPago":null,"fechaAcreditacion":null,
                 "importePagado":0,"payment":null,"chequeraCuota":null,"reservaVacante":null}
                """, encoder.bodyJson(), true);

        target(ContextoCoreClient.class).updateContexts(List.of(contexto));
        assertThat(http.last.httpMethod()).isEqualTo(Request.HttpMethod.PUT);
        assertThat(http.last.url()).isEqualTo(base(ContextoCoreClient.class) + "/updateContexts");
        assertThat(encoder.bodyJson()).startsWith("[").contains("\"mercadoPagoContextId\":5");
    }

    @Test
    void tipoChequeraTarjetaMantieneSuRuta() {
        target(TipoChequeraMercadoPagoCreditCardClient.class).findByUnique(1, 2);
        assertThat(http.last.httpMethod()).isEqualTo(Request.HttpMethod.GET);
        assertThat(http.last.url()).isEqualTo(base(TipoChequeraMercadoPagoCreditCardClient.class) + "/unique/1/2");
    }

    private <T> T target(Class<T> type) {
        return feign.target(type, base(type));
    }

    private void assertClient(Class<?> type, String path, String contextId) {
        FeignClient annotation = type.getAnnotation(FeignClient.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("tesoreria-core-service");
        assertThat(annotation.path()).isEqualTo(path);
        assertThat(annotation.contextId()).isEqualTo(contextId);
    }

    private String base(Class<?> type) {
        FeignClient annotation = type.getAnnotation(FeignClient.class);
        return "http://" + annotation.name() + annotation.path();
    }

    private static final class RecordingClient implements Client {
        private Request last;

        @Override
        public Response execute(Request request, Request.Options options) {
            this.last = request;
            return Response.builder().status(200).reason("OK").request(request).headers(Map.of()).build();
        }
    }

    private static final class RecordingEncoder implements Encoder {
        private Object lastBody;

        @Override
        public void encode(Object object, java.lang.reflect.Type bodyType, feign.RequestTemplate template) {
            this.lastBody = object;
        }

        String bodyJson() throws Exception {
            return MAPPER.writeValueAsString(lastBody);
        }
    }

}
