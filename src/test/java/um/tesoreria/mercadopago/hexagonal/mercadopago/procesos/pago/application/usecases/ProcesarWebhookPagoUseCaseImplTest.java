package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProcesarWebhookPagoUseCaseImplTest {

    private static final String SECRET = "secret-key";
    private static final String TS = "1764432770";
    private static final String REQUEST_ID = "777b047c-2103-40b2-ad8a-2bbf4843a542";

    @Mock
    private RecuperarYPublicarPagoUseCase recuperarYPublicarPagoUseCase;

    private ProcesarWebhookPagoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcesarWebhookPagoUseCaseImpl(recuperarYPublicarPagoUseCase);
        ReflectionTestUtils.setField(useCase, "secretKey", SECRET);
    }

    @Test
    void sinDataIdDevuelveElMensajeExacto() {
        assertThat(useCase.procesar(cabeceras("ts=1,v1=x", REQUEST_ID), null))
                .isEqualTo("Falta el parámetro data.id en la URL");
        assertThat(useCase.procesar(cabeceras("ts=1,v1=x", REQUEST_ID), ""))
                .isEqualTo("Falta el parámetro data.id en la URL");
        verifyNoInteractions(recuperarYPublicarPagoUseCase);
    }

    @Test
    void sinFirmaDevuelveElMensajeExacto() {
        assertThat(useCase.procesar(cabeceras(null, REQUEST_ID), "12345"))
                .isEqualTo("Falta el header x-signature");
    }

    @Test
    void sinRequestIdDevuelveElMensajeExacto() {
        assertThat(useCase.procesar(cabeceras("ts=1,v1=x", null), "12345"))
                .isEqualTo("Falta el header x-request-id");
    }

    @Test
    void conFirmaMalFormadaDevuelveElMensajeExacto() {
        assertThat(useCase.procesar(cabeceras("ts=1", REQUEST_ID), "12345"))
                .isEqualTo("Formato inválido en el header x-signature");
    }

    @Test
    void conFirmaValidaProcesaElPago() {
        String manifest = manifest("12345");

        assertThat(useCase.procesar(cabeceras("ts=" + TS + ",v1=" + hmac(manifest), REQUEST_ID), "12345"))
                .isEqualTo("\n\nPayment processed\n\n");

        verify(recuperarYPublicarPagoUseCase).recuperarYPublicar("12345");
    }

    @Test
    void conFirmaInvalidaIgualProcesaElPago() {
        assertThat(useCase.procesar(cabeceras("ts=" + TS + ",v1=deadbeef", REQUEST_ID), "12345"))
                .isEqualTo("\n\nPayment processed\n\n");

        verify(recuperarYPublicarPagoUseCase).recuperarYPublicar("12345");
    }

    private CabecerasWebhook cabeceras(String signature, String requestId) {
        return new CabecerasWebhook(signature, requestId);
    }

    private String manifest(String dataId) {
        return "id:" + dataId + ";request-id:" + REQUEST_ID + ";ts:" + TS + ";";
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
