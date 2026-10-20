package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;

public interface RecuperarPreferenciaCuotaUseCase {

    PreferenciaMercadoPago recuperar(String preferenceId);

}
