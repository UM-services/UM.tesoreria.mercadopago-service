package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.audit.AuditEventPublisher;
import um.tesoreria.mercadopago.audit.PreferenceSnapshot;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.service.ChequeraCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.application.service.TipoChequeraTarjetaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.exception.MercadoPagoException;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service.MercadoPagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.ActualizarPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.application.service.PreferenciaRequestBuilder;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActualizarPreferenciaCuotaUseCaseImpl implements ActualizarPreferenciaCuotaUseCase {

    private final MercadoPagoService mercadoPagoService;
    private final ChequeraCuotaService chequeraCuotaService;
    private final TipoChequeraTarjetaService tipoChequeraTarjetaService;
    private final PreferenciaRequestBuilder preferenciaRequestBuilder;
    private final AuditEventPublisher audit;

    @Override
    public ContextoMercadoPago actualizar(ContextoMercadoPago contexto) {
        return actualizar(contexto, null);
    }

    @Override
    public ContextoMercadoPago actualizar(ContextoMercadoPago contexto, ChequeraCuota cuotaExistente) {
        log.debug("\n\nProcessing ActualizarPreferenciaCuotaUseCaseImpl.actualizar\n\n");

        var antes = PreferenceSnapshot.of(contexto);

        var preferencia = mercadoPagoService.recuperar(contexto.getPreferenceId()).orElse(null);
        if (preferencia == null) {
            audit.publishPreferenceFailed("UPDATE", contexto.getPreferenceId(),
                    "preferencia no encontrada en Mercado Pago");
            return null;
        }

        var cuota = cuotaExistente != null ? cuotaExistente : chequeraCuotaService.getById(contexto.getChequeraCuotaId());
        var tipo = tipoChequeraTarjetaService
                .getByUnique(cuota.getTipoChequeraId(), cuota.getAlternativaId())
                .orElse(null);

        var solicitud = preferenciaRequestBuilder.construirParaActualizacion(contexto, preferencia, tipo);

        try {
            var actualizada = mercadoPagoService.actualizar(contexto.getPreferenceId(), solicitud);
            contexto.setChanged((byte) 0);
            if (actualizada != null) {
                contexto.setPreference(actualizada.getJson());
            }
            contexto.setLastVencimientoUpdated(OffsetDateTime.now(ZoneOffset.UTC));

            var despues = PreferenceSnapshot.of(contexto);
            audit.publishPreferenceUpdated(contexto.getPreferenceId(),
                    PreferenceSnapshot.diff(antes, despues), antes, despues);
        } catch (MercadoPagoException e) {
            log.debug("\n\nError al actualizar la preferencia: {}\n\n", e.getMessage());
            audit.publishPreferenceFailed("UPDATE", contexto.getPreferenceId(), e.getMessage());
        }

        return contexto;
    }

}
