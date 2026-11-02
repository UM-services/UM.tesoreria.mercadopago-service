package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class FirmaWebhookTest {

    private static final String SECRET = "secret-key";
    private static final String MANIFEST = "id:12345;request-id:777b047c-2103-40b2-ad8a-2bbf4843a542;ts:1764432770;";

    @Test
    void aceptaLaFirmaHmacValida() {
        assertThat(FirmaWebhook.validar(SECRET, MANIFEST, hmac(SECRET, MANIFEST))).isTrue();
    }

    @Test
    void aceptaLaFirmaEnMayusculas() {
        assertThat(FirmaWebhook.validar(SECRET, MANIFEST, hmac(SECRET, MANIFEST).toUpperCase())).isTrue();
    }

    @Test
    void rechazaUnaFirmaDeOtroSecreto() {
        assertThat(FirmaWebhook.validar(SECRET, MANIFEST, hmac("otro-secreto", MANIFEST))).isFalse();
    }

    @Test
    void rechazaUnaFirmaManipulada() {
        assertThat(FirmaWebhook.validar(SECRET, MANIFEST + "x", hmac(SECRET, MANIFEST))).isFalse();
    }

    @Test
    void rechazaUnaFirmaConFormatoInvalido() {
        assertThat(FirmaWebhook.validar(SECRET, MANIFEST, "no-es-hex")).isFalse();
        assertThat(FirmaWebhook.validar(SECRET, MANIFEST, "abc")).isFalse();
    }

    @Test
    void rechazaCuandoFaltaElSecretoOElManifest() {
        assertThat(FirmaWebhook.validar(null, MANIFEST, hmac(SECRET, MANIFEST))).isFalse();
        assertThat(FirmaWebhook.validar("", MANIFEST, hmac(SECRET, MANIFEST))).isFalse();
        assertThat(FirmaWebhook.validar(SECRET, null, hmac(SECRET, MANIFEST))).isFalse();
        assertThat(FirmaWebhook.validar(SECRET, MANIFEST, null)).isFalse();
    }

    private String hmac(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
