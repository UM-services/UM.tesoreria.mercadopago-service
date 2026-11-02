package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.domain.ports.in.CrearContextosChequeraBulkUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.domain.ports.in.CrearContextosChequeraUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChequeraService {

    private final CrearContextosChequeraUseCase crearContextosChequeraUseCase;
    private final CrearContextosChequeraBulkUseCase crearContextosChequeraBulkUseCase;

    public List<ContextoConCuota> createChequeraContext(Integer facultadId,
                                                       Integer tipoChequeraId,
                                                       Long chequeraSerieId,
                                                       Integer alternativaId) {
        return crearContextosChequeraUseCase.crear(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

    public List<ContextoConCuota> createChequeraContextBulk(Integer facultadId,
                                                            Integer tipoChequeraId,
                                                            Long chequeraSerieId,
                                                            Integer alternativaId) {
        return crearContextosChequeraBulkUseCase.crear(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
    }

}
