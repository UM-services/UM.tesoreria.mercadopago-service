import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class SignatureTest {
    public static void main(String[] args) throws Exception {
        String secretKey = "53bc1e532f1d267273cf0791aa060d024a048d85cde1dc12d24f59e2c12a5c9d";
        String manifest = "id:135812749740;request-id:777b047c-2103-40b2-ad8a-2bbf4843a542;ts:1764432770;";
        String expectedSignature = "f9ae7f41ad9d50d3fa4caa6181b5f9d593ec52401116af474f4b09ec78138d9a";

        System.out.println("Testing with Secret as String...");
        String sig1 = calculateHmac(secretKey.getBytes(StandardCharsets.UTF_8), manifest);
        System.out.println("Signature 1: " + sig1);
        System.out.println("Match? " + sig1.equals(expectedSignature));

        System.out.println("\nTesting with Secret as Hex Decoded Bytes...");
        byte[] secretBytes = HexFormat.of().parseHex(secretKey);
        String sig2 = calculateHmac(secretBytes, manifest);
        System.out.println("Signature 2: " + sig2);
        System.out.println("Match? " + sig2.equals(expectedSignature));
    }

    private static String calculateHmac(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hmacData);
    }
}
