package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.ActualizarContextosUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ActualizarContextosUseCaseImpl implements ActualizarContextosUseCase {

    private final ContextoRepository contextoRepository;

    @Override
    public List<ContextoMercadoPago> actualizar(List<ContextoMercadoPago> contextos) {
        return contextoRepository.actualizarTodos(contextos);
    }

}
