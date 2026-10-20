package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.application.service.TipoChequeraTarjetaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.exception.MercadoPagoException;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service.MercadoPagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.ActualizarPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.CrearPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.application.service.PreferenciaRequestBuilder;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrearPreferenciaCuotaUseCaseImpl implements CrearPreferenciaCuotaUseCase {

    private final MercadoPagoService mercadoPagoService;
    private final TipoChequeraTarjetaService tipoChequeraTarjetaService;
    private final PreferenciaRequestBuilder preferenciaRequestBuilder;
    private final ActualizarPreferenciaCuotaUseCase actualizarPreferenciaCuotaUseCase;

    @Override
    public ContextoMercadoPago crear(ContextoMercadoPago contexto, ChequeraCuota cuota) {
        log.debug("\n\nProcessing CrearPreferenciaCuotaUseCaseImpl.crear\n\n");
        if (contexto == null) {
            throw new IllegalArgumentException("UMPreferenceMPDto cannot be null");
        }

        if (contexto.getInitPoint() != null) {
            if (contexto.getChanged() == 1) {
                return actualizarPreferenciaCuotaUseCase.actualizar(contexto);
            }
            return contexto;
        }

        var tipo = tipoChequeraTarjetaService
                .getByUnique(cuota.getTipoChequeraId(), cuota.getAlternativaId())
                .orElse(null);

        var solicitud = preferenciaRequestBuilder.construirParaCuota(contexto, cuota, tipo);

        try {
            var preferencia = mercadoPagoService.crear(solicitud);
            contexto.setInitPoint(preferencia.getInitPoint());
            contexto.setPreferenceId(preferencia.getId());
            contexto.setPreference(preferencia.getJson());
        } catch (MercadoPagoException e) {
            log.error("MercadoPago API Error -> Status: {}, Message: {}, Response: {}",
                    e.getStatusCode(), e.getMessage(), e.getApiResponse());
        }

        return contexto;
    }

}
