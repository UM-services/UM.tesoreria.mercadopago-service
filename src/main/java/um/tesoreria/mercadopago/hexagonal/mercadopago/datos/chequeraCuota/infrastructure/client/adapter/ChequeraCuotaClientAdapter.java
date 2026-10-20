package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.infrastructure.client.adapter;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.domain.ports.out.ChequeraCuotaRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.infrastructure.client.ChequeraCuotaClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ChequeraCuotaDtoMapper;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChequeraCuotaClientAdapter implements ChequeraCuotaRepository {

    private final ChequeraCuotaClient chequeraCuotaClient;
    private final ChequeraCuotaDtoMapper chequeraCuotaDtoMapper;

    @Override
    public List<ChequeraCuota> findPendientes(Integer facultadId,
                                              Integer tipoChequeraId,
                                              Long chequeraSerieId,
                                              Integer alternativaId) {
        return chequeraCuotaClient.findAllPendientes(facultadId, tipoChequeraId, chequeraSerieId, alternativaId)
                .stream()
                .map(chequeraCuotaDtoMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ChequeraCuota> findByChequeraCuotaId(Long chequeraCuotaId) {
        try {
            return Optional.ofNullable(chequeraCuotaDtoMapper.toDomain(
                    chequeraCuotaClient.findByChequeraCuotaId(chequeraCuotaId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
