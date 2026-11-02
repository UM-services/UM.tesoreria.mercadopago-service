package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.in.CrearContextosChequeraUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.domain.ports.out.ContextoRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CrearContextosChequeraContextoUseCaseImpl implements CrearContextosChequeraUseCase {

    private final ContextoRepository contextoRepository;

    @Override
    public List<ContextoConCuota> crear(Integer facultadId,
                                        Integer tipoChequeraId,
                                        Long chequeraSerieId,
                                        Integer alternativaId) {
        return contextoRepository.crearContextosChequera(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

}
