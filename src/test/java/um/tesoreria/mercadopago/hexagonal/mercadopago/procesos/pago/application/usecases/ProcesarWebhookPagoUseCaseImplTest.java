package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.EstadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.OrigenNotificacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        var resultado = useCase.procesar(cabeceras("ts=1,v1=x", REQUEST_ID), null);

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.DATA_ID_FALTANTE);
        assertThat(resultado.mensaje()).isEqualTo("Falta el parámetro data.id en la URL");
        assertThat(useCase.procesar(cabeceras("ts=1,v1=x", REQUEST_ID), "").estado())
                .isEqualTo(EstadoWebhookPago.DATA_ID_FALTANTE);
        verifyNoInteractions(recuperarYPublicarPagoUseCase);
    }

    @Test
    void sinFirmaDevuelveFirmaFaltante() {
        var resultado = useCase.procesar(cabeceras(null, REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.FIRMA_FALTANTE);
        assertThat(resultado.mensaje()).isEqualTo("Falta el header x-signature");
    }

    @Test
    void sinRequestIdDevuelveRequestIdFaltante() {
        var resultado = useCase.procesar(cabeceras("ts=1,v1=x", null), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.REQUEST_ID_FALTANTE);
        assertThat(resultado.mensaje()).isEqualTo("Falta el header x-request-id");
    }

    @Test
    void conFirmaMalFormadaDevuelveElMensajeExacto() {
        var resultado = useCase.procesar(cabeceras("ts=1", REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.FIRMA_MAL_FORMADA);
        assertThat(resultado.mensaje()).isEqualTo("Formato inválido en el header x-signature");
    }

    @Test
    void conFirmaValidaProcesaElPago() {
        String manifest = manifest("12345");
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1)).thenReturn(true);

        var resultado = useCase.procesar(cabeceras("ts=" + TS + ",v1=" + hmac(manifest), REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.PROCESADO);
        assertThat(resultado.mensaje()).isEqualTo("\n\nPayment processed\n\n");
        assertThat(resultado.exitoso()).isTrue();
        verify(recuperarYPublicarPagoUseCase).recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1);
    }

    @Test
    void conFirmaInvalidaRechazaElPago() {
        var resultado = useCase.procesar(cabeceras("ts=" + TS + ",v1=deadbeef", REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.FIRMA_INVALIDA);
        assertThat(resultado.mensaje()).isEqualTo("Firma inválida");
        verifyNoInteractions(recuperarYPublicarPagoUseCase);
    }

    @Test
    void conFirmaDeOtroSecretoRechazaElPago() {
        String manifest = manifest("12345");

        var resultado = useCase.procesar(
                cabeceras("ts=" + TS + ",v1=" + hmac(manifest, "otro-secreto"), REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.FIRMA_INVALIDA);
        verifyNoInteractions(recuperarYPublicarPagoUseCase);
    }

    @Test
    void conFirmaValidaEnMayusculasProcesaElPago() {
        String manifest = manifest("12345");
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1)).thenReturn(true);

        var resultado = useCase.procesar(
                cabeceras("ts=" + TS + ",v1=" + hmac(manifest).toUpperCase(), REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.PROCESADO);
        verify(recuperarYPublicarPagoUseCase).recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1);
    }

    @Test
    void sinSecretKeyConfiguradaRechazaElPago() {
        ReflectionTestUtils.setField(useCase, "secretKey", "");

        var resultado = useCase.procesar(
                cabeceras("ts=" + TS + ",v1=" + hmac(manifest("12345")), REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.FIRMA_INVALIDA);
        verifyNoInteractions(recuperarYPublicarPagoUseCase);
    }

    @Test
    void conFirmaValidaPeroPagoNoRecuperableDevuelvePagoNoEncontrado() {
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1)).thenReturn(false);

        var resultado = useCase.procesar(
                cabeceras("ts=" + TS + ",v1=" + hmac(manifest("12345")), REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.PAGO_NO_ENCONTRADO);
        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.mensaje()).isEqualTo("No se pudo recuperar el pago desde Mercado Pago");
    }

    @Test
    void conDataIdNoNumericoDevuelveDataIdInvalido() {
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("abc", OrigenNotificacion.WEBHOOK_V1))
                .thenThrow(new NumberFormatException("For input string: \"abc\""));

        var resultado = useCase.procesar(
                cabeceras("ts=" + TS + ",v1=" + hmac(manifest("abc")), REQUEST_ID), "abc");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.DATA_ID_INVALIDO);
        assertThat(resultado.mensaje()).isEqualTo("Parámetro data.id inválido");
    }

    @Test
    void laNotificacionLegadaProcesaElPagoSinValidarFirma() {
        ReflectionTestUtils.setField(useCase, "legacyIpnHabilitado", true);
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("12345", OrigenNotificacion.IPN_LEGACY))
                .thenReturn(true);

        var resultado = useCase.procesarNotificacionLegada("12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.PROCESADO);
        verify(recuperarYPublicarPagoUseCase).recuperarYPublicar("12345", OrigenNotificacion.IPN_LEGACY);
    }

    @Test
    void laNotificacionLegadaApagadaSeIgnoraSinPublicar() {
        // legacyIpnHabilitado queda null (default false de app.webhook.legacy-ipn-enabled)
        var resultado = useCase.procesarNotificacionLegada("12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.LEGACY_IGNORADA);
        assertThat(resultado.mensaje()).isEqualTo("Notificación legacy ignorada");
        verifyNoInteractions(recuperarYPublicarPagoUseCase);
    }

    @Test
    void laNotificacionLegadaApagadaSeIgnoraAunqueElPagoExista() {
        ReflectionTestUtils.setField(useCase, "legacyIpnHabilitado", false);

        var resultado = useCase.procesarNotificacionLegada("12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.LEGACY_IGNORADA);
        verifyNoInteractions(recuperarYPublicarPagoUseCase);
    }

    @Test
    void laNotificacionLegadaSinIdDevuelveDataIdFaltante() {
        assertThat(useCase.procesarNotificacionLegada(null).estado())
                .isEqualTo(EstadoWebhookPago.DATA_ID_FALTANTE);
        assertThat(useCase.procesarNotificacionLegada("").estado())
                .isEqualTo(EstadoWebhookPago.DATA_ID_FALTANTE);
        verifyNoInteractions(recuperarYPublicarPagoUseCase);
    }

    @Test
    void conValidacionDeshabilitadaProcesaAunqueLaFirmaSeaInvalida() {
        ReflectionTestUtils.setField(useCase, "validarFirma", false);
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1)).thenReturn(true);

        var resultado = useCase.procesar(cabeceras("ts=" + TS + ",v1=deadbeef", REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.PROCESADO);
        verify(recuperarYPublicarPagoUseCase).recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1);
    }

    @Test
    void conValidacionDeshabilitadaProcesaSinCabeceras() {
        ReflectionTestUtils.setField(useCase, "validarFirma", false);
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1)).thenReturn(true);

        var resultado = useCase.procesar(cabeceras(null, null), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.PROCESADO);
        verify(recuperarYPublicarPagoUseCase).recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1);
    }

    @Test
    void conValidacionDeshabilitadaYFirmaMalFormadaProcesaIgual() {
        ReflectionTestUtils.setField(useCase, "validarFirma", false);
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1)).thenReturn(true);

        var resultado = useCase.procesar(cabeceras("ts=1", REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.PROCESADO);
    }

    @Test
    void conValidacionDeshabilitadaElPagoNoEncontradoSigueSiendoError() {
        ReflectionTestUtils.setField(useCase, "validarFirma", false);
        when(recuperarYPublicarPagoUseCase.recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1)).thenReturn(false);

        var resultado = useCase.procesar(cabeceras("ts=" + TS + ",v1=deadbeef", REQUEST_ID), "12345");

        assertThat(resultado.estado()).isEqualTo(EstadoWebhookPago.PAGO_NO_ENCONTRADO);
    }

    private CabecerasWebhook cabeceras(String signature, String requestId) {
        return new CabecerasWebhook(signature, requestId);
    }

    private String manifest(String dataId) {
        return "id:" + dataId + ";request-id:" + REQUEST_ID + ";ts:" + TS + ";";
    }

    private String hmac(String data) {
        return hmac(data, SECRET);
    }

    private String hmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
