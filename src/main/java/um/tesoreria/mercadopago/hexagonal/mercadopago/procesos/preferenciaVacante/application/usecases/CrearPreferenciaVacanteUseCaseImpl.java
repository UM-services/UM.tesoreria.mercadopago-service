package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.audit.AuditEventPublisher;
import um.tesoreria.mercadopago.audit.PreferenceSnapshot;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.exception.MercadoPagoException;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service.MercadoPagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.domain.ports.in.CrearPreferenciaVacanteUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.application.service.PreferenciaRequestBuilder;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ReservaVacante;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrearPreferenciaVacanteUseCaseImpl implements CrearPreferenciaVacanteUseCase {

    private final MercadoPagoService mercadoPagoService;
    private final ContextoService contextoService;
    private final PreferenciaRequestBuilder preferenciaRequestBuilder;
    private final AuditEventPublisher audit;

    @Override
    public ContextoMercadoPago crear(ContextoMercadoPago contexto, ReservaVacante reserva) {
        log.debug("\n\nProcessing CrearPreferenciaVacanteUseCaseImpl.crear\n\n");
        if (contexto == null) {
            throw new IllegalArgumentException("UMPreferenceMPDto cannot be null");
        }
        if (contexto.getInitPoint() != null) {
            return contexto;
        }

        var solicitud = preferenciaRequestBuilder.construirParaReserva(contexto, reserva);

        try {
            var preferencia = mercadoPagoService.crear(solicitud);
            contexto.setInitPoint(preferencia.getInitPoint());
            contexto.setPreferenceId(preferencia.getId());
            contexto.setPreference(preferencia.getJson());
            contextoService.actualizar(contexto);
            audit.publishPreferenceCreated(preferencia.getId(), PreferenceSnapshot.of(contexto));
        } catch (MercadoPagoException e) {
            log.error("MercadoPago API Error -> Status: {}, Message: {}, Response: {}",
                    e.getStatusCode(), e.getMessage(), e.getApiResponse());
            audit.publishPreferenceFailed("INSERT", contexto.getPreferenceId(), e.getMessage());
        }

        return contexto;
    }

}
