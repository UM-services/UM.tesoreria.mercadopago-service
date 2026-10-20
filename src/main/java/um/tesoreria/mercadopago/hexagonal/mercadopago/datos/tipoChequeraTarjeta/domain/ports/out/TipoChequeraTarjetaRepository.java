package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.domain.ports.out;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;

import java.util.Optional;

public interface TipoChequeraTarjetaRepository {

    Optional<TipoChequeraTarjeta> findByUnique(Integer tipoChequeraId, Integer alternativaId);

}
