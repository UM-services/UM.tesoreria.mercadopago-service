package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.GetContextosActivosUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetContextosActivosUseCaseImpl implements GetContextosActivosUseCase {

    private final ContextoRepository contextoRepository;

    @Override
    public List<Long> getActivosParaCambiar() {
        return contextoRepository.findActivosParaCambiar();
    }

}
