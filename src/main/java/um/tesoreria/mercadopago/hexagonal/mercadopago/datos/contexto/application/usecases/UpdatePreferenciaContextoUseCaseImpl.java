package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.UpdatePreferenciaContextoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

@Component
@RequiredArgsConstructor
public class UpdatePreferenciaContextoUseCaseImpl implements UpdatePreferenciaContextoUseCase {

    private final ContextoRepository contextoRepository;

    @Override
    public void actualizarPreferencia(ContextoMercadoPago contexto) {
        contextoRepository.actualizarPreferencia(contexto);
    }

    @Override
    public void marcarChanged(Long mercadoPagoContextId, byte changed) {
        contextoRepository.marcarChanged(mercadoPagoContextId, changed);
    }
}
