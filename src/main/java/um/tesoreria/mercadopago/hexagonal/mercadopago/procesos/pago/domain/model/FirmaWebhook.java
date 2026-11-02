package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class FirmaWebhook {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private FirmaWebhook() {
    }

    public static boolean validar(String secretKey, String manifest, String firmaEsperada) {
        if (secretKey == null || secretKey.isEmpty() || manifest == null || firmaEsperada == null) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] firmaCalculada = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            byte[] firmaRecibida = HexFormat.of().parseHex(firmaEsperada.trim());
            return MessageDigest.isEqual(firmaCalculada, firmaRecibida);
        } catch (Exception e) {
            return false;
        }
    }

}
