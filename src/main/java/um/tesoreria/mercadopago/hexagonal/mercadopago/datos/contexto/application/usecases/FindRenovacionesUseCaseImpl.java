package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.FindRenovacionesUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.CandidatoRenovacion;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FindRenovacionesUseCaseImpl implements FindRenovacionesUseCase {

    private final ContextoRepository contextoRepository;

    @Override
    public List<CandidatoRenovacion> findRenovaciones(int lookbackDias, boolean reintentos,
                                                     OffsetDateTime afterFecha, Long afterId, int size) {
        return contextoRepository.findRenovaciones(lookbackDias, reintentos, afterFecha, afterId, size)
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
