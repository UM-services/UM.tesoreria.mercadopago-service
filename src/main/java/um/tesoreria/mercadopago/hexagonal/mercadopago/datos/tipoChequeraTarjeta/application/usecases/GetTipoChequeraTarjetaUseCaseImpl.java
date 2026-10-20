package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.domain.ports.in.GetTipoChequeraTarjetaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.domain.ports.out.TipoChequeraTarjetaRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTipoChequeraTarjetaUseCaseImpl implements GetTipoChequeraTarjetaUseCase {

    private final TipoChequeraTarjetaRepository tipoChequeraTarjetaRepository;

    @Override
    public Optional<TipoChequeraTarjeta> getByUnique(Integer tipoChequeraId, Integer alternativaId) {
        try {
            TipoChequeraTarjeta tipo = tipoChequeraTarjetaRepository
                    .findByUnique(tipoChequeraId, alternativaId)
                    .orElse(null);
            if (tipo == null || tipo.getActive() == 0) {
                return Optional.empty();
            }
            if (Objects.equals(tipo.getAlternativaId(), alternativaId)) {
                return Optional.of(tipo);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.debug("Error al obtener el contexto de tipo de chequera: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
