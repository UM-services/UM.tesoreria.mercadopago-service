package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.infrastructure.client.adapter;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.domain.ports.out.TipoChequeraTarjetaRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.infrastructure.client.TipoChequeraMercadoPagoCreditCardClient;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.TipoChequeraTarjetaDtoMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TipoChequeraTarjetaClientAdapter implements TipoChequeraTarjetaRepository {

    private final TipoChequeraMercadoPagoCreditCardClient tipoChequeraMercadoPagoCreditCardClient;
    private final TipoChequeraTarjetaDtoMapper tipoChequeraTarjetaDtoMapper;

    @Override
    public Optional<TipoChequeraTarjeta> findByUnique(Integer tipoChequeraId, Integer alternativaId) {
        try {
            return Optional.ofNullable(tipoChequeraTarjetaDtoMapper.toDomain(
                    tipoChequeraMercadoPagoCreditCardClient.findByUnique(tipoChequeraId, alternativaId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
