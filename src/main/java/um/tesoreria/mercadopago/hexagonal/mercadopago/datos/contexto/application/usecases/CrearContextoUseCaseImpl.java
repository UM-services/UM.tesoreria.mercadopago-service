package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.CrearContextoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;

@Component
@RequiredArgsConstructor
public class CrearContextoUseCaseImpl implements CrearContextoUseCase {

    private final ContextoRepository contextoRepository;

    @Override
    public ContextoConCuota crear(Long chequeraCuotaId) {
        return contextoRepository.crearContexto(chequeraCuotaId);
    }

}
