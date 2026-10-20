package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.domain.ports.in.CrearPreferenciaVacanteUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ReservaVacante;

@Service
@RequiredArgsConstructor
public class PreferenciaVacanteService {

    private final CrearPreferenciaVacanteUseCase crearPreferenciaVacanteUseCase;

    public ContextoMercadoPago crear(ContextoMercadoPago contexto, ReservaVacante reserva) {
        return crearPreferenciaVacanteUseCase.crear(contexto, reserva);
    }

}
