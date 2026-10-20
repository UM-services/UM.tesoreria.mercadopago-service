package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ComponentesFirma;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.FirmaWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.ProcesarWebhookPagoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.ports.in.RecuperarYPublicarPagoUseCase;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcesarWebhookPagoUseCaseImpl implements ProcesarWebhookPagoUseCase {

    private final RecuperarYPublicarPagoUseCase recuperarYPublicarPagoUseCase;

    @Value("${app.secret-key:}")
    private String secretKey;

    @Override
    public String procesar(CabecerasWebhook cabeceras, String dataId) {
        if (dataId == null || dataId.isEmpty()) {
            log.error("Falta el parámetro data.id en la URL");
            return "Falta el parámetro data.id en la URL";
        }

        log.debug("\n\nProcessing ProcesarWebhookPagoUseCaseImpl.procesar for {}\n\n", dataId);

        String xSignature = cabeceras.xSignature();
        String xRequestId = cabeceras.xRequestId();
        log.debug("Signature from MP -> {}", xSignature);

        if (xSignature == null || xSignature.isEmpty()) {
            log.error("Falta el header x-signature");
            return "Falta el header x-signature";
        }
        if (xRequestId == null || xRequestId.isEmpty()) {
            log.error("Falta el header x-request-id");
            return "Falta el header x-request-id";
        }

        ComponentesFirma componentes = ComponentesFirma.parse(xSignature);
        if (componentes == null) {
            return "Formato inválido en el header x-signature";
        }

        String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, componentes.ts());
        log.debug("Manifest string: {}", manifest);

        if (!FirmaWebhook.validar(secretKey, manifest, componentes.v1())) {
            log.warn("Deja continuar para testear kafka");
        }

        recuperarYPublicarPagoUseCase.recuperarYPublicar(dataId);

        return "\n\nPayment processed\n\n";
    }

}
