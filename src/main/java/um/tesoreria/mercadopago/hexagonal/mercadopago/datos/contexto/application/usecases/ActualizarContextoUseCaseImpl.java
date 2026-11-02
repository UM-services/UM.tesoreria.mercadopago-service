package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.ActualizarContextoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

@Component
@RequiredArgsConstructor
public class ActualizarContextoUseCaseImpl implements ActualizarContextoUseCase {

    private final ContextoRepository contextoRepository;

    @Override
    public ContextoMercadoPago actualizar(ContextoMercadoPago contexto) {
        return contextoRepository.actualizar(contexto);
    }

}
