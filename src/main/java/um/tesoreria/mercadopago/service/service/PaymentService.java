package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoCoreClient;
import um.tesoreria.mercadopago.service.client.core.PagoClient;
import um.tesoreria.mercadopago.service.domain.dto.MercadoPagoContextDto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Service
@Slf4j
public class PaymentService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String EXTERNAL_REFERENCE_SEPARATOR = "-";
    private static final int EXPECTED_PARTS_LENGTH = 3;

    @Value("${app.secret-key}")
    private String secretKey;

    @Value("${app.access-token}")
    private String accessToken;

    private final MercadoPagoCoreClient mercadoPagoCoreClient;
    private final PagoClient pagoClient;

    public PaymentService(MercadoPagoCoreClient mercadoPagoCoreClient,
                          PagoClient pagoClient) {
        this.mercadoPagoCoreClient = mercadoPagoCoreClient;
        this.pagoClient = pagoClient;
    }

    /**
     * Procesa el webhook de pago recibido desde MercadoPago
     * @param request La solicitud HTTP recibida
     * @param dataId El ID del pago a procesar
     * @return Mensaje indicando el resultado del procesamiento
     */
    public String processPaymentWebhook(HttpServletRequest request, String dataId) {
        if (dataId == null || dataId.isEmpty()) {
            log.error("Falta el parámetro data.id en la URL");
            return "Falta el parámetro data.id en la URL";
        }

        String xSignature = request.getHeader("x-signature");
        String xRequestId = request.getHeader("x-request-id");

        log.debug("Signature from MP -> {}", xSignature);

        String headerValidationResult = validateHeaders(xSignature, xRequestId);
        if (headerValidationResult != null) {
            return headerValidationResult;
        }

        SignatureComponents signatureComponents = extractSignatureComponents(xSignature);
        if (signatureComponents == null) {
            return "Formato inválido en el header x-signature";
        }

        String manifest = buildManifest(dataId, xRequestId, signatureComponents.ts);
        log.debug("Manifest string: {}", manifest);

        if (!verifySignature(manifest, signatureComponents.v1)) {
            return "Verificación fallida";
        }

        retrieveAndSavePayment(dataId);

        return "Payment processed";
    }

    /**
     * Recupera y guarda la información del pago
     * @param dataId ID del pago en MercadoPago
     * @return El objeto Payment procesado
     */
    public Payment retrieveAndSavePayment(String dataId) {
        log.debug("Processing retrieveAndSavePayment");
        PaymentClient client = new PaymentClient();
        Payment payment;

        try {
            MPRequestOptions requestOptions = MPRequestOptions.builder()
                    .accessToken(accessToken)
                    .build();

            payment = client.get(Long.parseLong(dataId), requestOptions);
            logPayment(payment);
        } catch (MPException | MPApiException e) {
            log.error("Error getting payment: {}", e.getMessage());
            return null;
        }

        var context = processPaymentContext(payment, dataId);
        log.debug("Context processed - status: {}", context.getStatus());

        if (Objects.equals(context.getStatus(), "approved")) {
            var chequeraPago = pagoClient.registrarPagoMercadoPago(context.getMercadoPagoContextId());
            try {
                log.debug("ChequeraPago -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(chequeraPago));
            } catch (JsonProcessingException e) {
                log.debug("ChequeraPago Error -> {}", e.getMessage());
            }
            context.setChequeraPagoId(chequeraPago.getChequeraPagoId());
            mercadoPagoCoreClient.updateContext(context, context.getMercadoPagoContextId());
        }

        if (context.getStatus().equals("approved") || context.getStatus().equals("rejected")) {
            context.setActivo((byte) 0);
            mercadoPagoCoreClient.updateContext(context, context.getMercadoPagoContextId());
        }

        return payment;
    }

    private String validateHeaders(String xSignature, String xRequestId) {
        log.debug("Processing validateHeaders");
        if (xSignature == null || xSignature.isEmpty()) {
            log.error("Falta el header x-signature");
            return "Falta el header x-signature";
        }
        if (xRequestId == null || xRequestId.isEmpty()) {
            log.error("Falta el header x-request-id");
            return "Falta el header x-request-id";
        }
        return null;
    }

    private SignatureComponents extractSignatureComponents(String xSignature) {
        log.debug("Processing extractSignatureComponents");
        String ts = null;
        String v1 = null;
        String[] parts = xSignature.split(",");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if ("ts".equals(key)) {
                    ts = value;
                } else if ("v1".equals(key)) {
                    v1 = value;
                }
            }
        }

        return (ts != null && v1 != null) ? new SignatureComponents(ts, v1) : null;
    }

    private String buildManifest(String dataId, String xRequestId, String ts) {
        log.debug("Processing buildManifest");
        return String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, ts);
    }

    private boolean verifySignature(String manifest, String expectedSignature) {
        log.debug("Processing verifySignature");
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacData = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hmacData);
            log.debug("Generated signature: {}", generatedSignature);

            return generatedSignature.equalsIgnoreCase(expectedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error al generar la firma HMAC", e);
            return false;
        }
    }

    private void logPayment(Payment payment) {
        log.debug("Processing logPayment");
        try {
            log.debug("Payment -> {}", JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payment));
        } catch (JsonProcessingException e) {
            log.error("Payment error {}", e.getMessage());
        }
    }

    private void logMercadoPagoContext(MercadoPagoContextDto mercadoPagoContext) {
        log.debug("Processing logMercadoPagoContext");
        try {
            log.debug("MercadoPagoContext -> {}", JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mercadoPagoContext));
        } catch (JsonProcessingException e) {
            log.error("MercadoPagoContext error {}", e.getMessage());
        }
    }

    private MercadoPagoContextDto processPaymentContext(Payment payment, String dataId) {
        log.debug("Processing processPaymentContext");
        if (payment == null) return null;

        String externalReference = payment.getExternalReference();
        PaymentReferenceData referenceData = parseExternalReference(externalReference);
        if (referenceData == null) return null;

        MercadoPagoContextDto mercadoPagoContext = mercadoPagoCoreClient
                .findContextByMercadoPagoContextId(referenceData.mercadoPagoContextId);
        logMercadoPagoContext(mercadoPagoContext);

        if (!Objects.equals(mercadoPagoContext.getChequeraCuotaId(), referenceData.chequeraCuotaId)) {
            log.debug("Inconsistencia de chequeraCuotaId entre MPContext y payment");
            return null;
        }

        return updateMercadoPagoContext(mercadoPagoContext, payment, dataId);
    }

    private PaymentReferenceData parseExternalReference(String externalReference) {
        log.debug("Processing parseExternalReference");
        String[] parts = externalReference.split(EXTERNAL_REFERENCE_SEPARATOR);
        if (parts.length == EXPECTED_PARTS_LENGTH) {
            try {
                Long chequeraCuotaId = Long.parseLong(parts[1]);
                log.debug("PaymentReferenceData - ChequeraCuotaId -> {}", chequeraCuotaId);
                Long mercadoPagoContextId = Long.parseLong(parts[2]);
                log.debug("PaymentReferenceData - MercadoPagoContextId -> {}", mercadoPagoContextId);
                return new PaymentReferenceData(chequeraCuotaId, mercadoPagoContextId);
            } catch (NumberFormatException e) {
                log.error("Error parsing reference numbers: {}", e.getMessage());
            }
        }
        log.error("Formato inválido de externalReference: {}", externalReference);
        return null;
    }

    private MercadoPagoContextDto updateMercadoPagoContext(MercadoPagoContextDto context, Payment payment, String dataId) {
        log.debug("Processing updateMercadoPagoContext");
        context.setIdMercadoPago(dataId);
        try {
            String paymentString = JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payment);
            context.setPayment(paymentString);
        } catch (JsonProcessingException e) {
            log.error("PaymentString Error -> {}", e.getMessage());
        }
        context.setImportePagado(payment.getTransactionDetails().getTotalPaidAmount());
        context.setFechaPago(payment.getDateApproved());
        context.setFechaAcreditacion(payment.getMoneyReleaseDate());
        context.setStatus(payment.getStatus());
        log.debug("Antes");
        logMercadoPagoContext(context);
        context = mercadoPagoCoreClient.updateContext(context, context.getMercadoPagoContextId());
        log.debug("Después");
        logMercadoPagoContext(context);
        return context;
    }

    private record SignatureComponents(String ts, String v1) {}
    private record PaymentReferenceData(Long chequeraCuotaId, Long mercadoPagoContextId) {}
}
