package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in.ActualizarPreferenciaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in.CrearPreferenciaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in.RecuperarPagoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.domain.ports.in.RecuperarPreferenciaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    private final CrearPreferenciaUseCase crearPreferenciaUseCase;
    private final ActualizarPreferenciaUseCase actualizarPreferenciaUseCase;
    private final RecuperarPreferenciaUseCase recuperarPreferenciaUseCase;
    private final RecuperarPagoUseCase recuperarPagoUseCase;

    public PreferenciaMercadoPago crear(SolicitudPreferencia solicitud) {
        return crearPreferenciaUseCase.crear(solicitud);
    }

    public PreferenciaMercadoPago actualizar(String preferenceId, SolicitudPreferencia solicitud) {
        return actualizarPreferenciaUseCase.actualizar(preferenceId, solicitud);
    }

    public Optional<PreferenciaMercadoPago> recuperar(String preferenceId) {
        return recuperarPreferenciaUseCase.recuperar(preferenceId);
    }

    public Optional<PagoMercadoPago> recuperarPago(Long paymentId) {
        return recuperarPagoUseCase.recuperar(paymentId);
    }

}
