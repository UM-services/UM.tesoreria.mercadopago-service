package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service.MercadoPagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.RecuperarPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;

@Component
@RequiredArgsConstructor
public class RecuperarPreferenciaCuotaUseCaseImpl implements RecuperarPreferenciaCuotaUseCase {

    private final MercadoPagoService mercadoPagoService;

    @Override
    public PreferenciaMercadoPago recuperar(String preferenceId) {
        return mercadoPagoService.recuperar(preferenceId).orElse(null);
    }

}
