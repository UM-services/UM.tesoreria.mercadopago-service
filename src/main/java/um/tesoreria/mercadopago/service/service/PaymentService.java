package um.tesoreria.mercadopago.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.service.domain.event.PaymentProcessedEvent;
import um.tesoreria.mercadopago.service.producer.PaymentEventProducer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String EXTERNAL_REFERENCE_SEPARATOR = "-";
    private static final int EXPECTED_PARTS_LENGTH = 3;

    @Value("${app.secret-key}")
    private String secretKey;

    @Value("${app.access-token}")
    private String accessToken;

    private final PaymentEventProducer paymentEventProducer;

    /**
     * Procesa el webhook de pago recibido desde MercadoPago
     * 
     * @param request La solicitud HTTP recibida
     * @param dataId  El ID del pago a procesar
     * @return Mensaje indicando el resultado del procesamiento
     */
    public String processPaymentWebhook(HttpServletRequest request, String dataId) {
        if (dataId == null || dataId.isEmpty()) {
            log.error("Falta el parámetro data.id en la URL");
            return "Falta el parámetro data.id en la URL";
        }

        log.debug("\n\nProcessing PaymentService.processPaymentWebhook for {}\n\n", dataId);

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
//            log.warn("Deja continuar para testear kafka");
             return "\n\nVerificación fallida\n\n";
        }

        retrieveAndPublishPayment(dataId);

        return "\n\nPayment processed\n\n";
    }

    /**
     * Recupera y publica la información del pago
     * 
     * @param dataId id del pago en MercadoPago
     */
    public void retrieveAndPublishPayment(String dataId) {
        log.debug("\n\nProcessing PaymentService.retrieveAndPublishPayment for {}\n\n", dataId);
        PaymentClient client = new PaymentClient();
        Payment payment;

        try {
            MPRequestOptions requestOptions = MPRequestOptions.builder()
                    .accessToken(accessToken)
                    .build();

            payment = client.get(Long.parseLong(dataId), requestOptions);
        } catch (MPException | MPApiException e) {
            log.error("Error getting payment for {}: {}", dataId, e.getMessage());
            return;
        }

        processPaymentContext(payment, dataId);
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
        log.debug("Processing PaymentService.buildManifest");
        return String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, ts);
    }

    private boolean verifySignature(String manifest, String expectedSignature) {
        log.debug("\n\nProcessing PaymentService.verifySignature\n\n");
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            log.debug("SecretKey: {}", secretKey);
            log.debug("ExpectedSignature: {}", expectedSignature);
            log.debug("Access token: {}", accessToken);
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

    private void processPaymentContext(Payment payment, String dataId) {
        log.debug("\n\nProcessing PaymentService.processPaymentContext\n\n");
        if (payment == null)
            return;

        String externalReference = payment.getExternalReference();
        PaymentReferenceData referenceData = parseExternalReference(externalReference);
        if (referenceData == null)
            return;

        log.debug("\n\nPublish Payment Event\n\n");
        publishPaymentEvent(payment, dataId, referenceData);
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

    private void publishPaymentEvent(Payment payment, String dataId, PaymentReferenceData referenceData) {
        log.debug("\n\nProcessing PaymentService.publishPaymentEvent\n\n");
        String paymentString = null;
        try {
            paymentString = JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            log.error("PaymentString Error -> {}", e.getMessage());
        }

        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .mercadoPagoContextId(referenceData.mercadoPagoContextId)
                .chequeraCuotaId(referenceData.chequeraCuotaId)
                .paymentId(dataId)
                .status(payment.getStatus())
                .statusDetail(payment.getStatusDetail())
                .dateApproved(payment.getDateApproved())
                .dateCreated(payment.getDateCreated())
                .transactionAmount(payment.getTransactionAmount())
                .paymentJson(paymentString)
                .build();

        paymentEventProducer.publish(event);
    }

    private record SignatureComponents(String ts, String v1) {
    }

    private record PaymentReferenceData(Long chequeraCuotaId, Long mercadoPagoContextId) {
    }
}
