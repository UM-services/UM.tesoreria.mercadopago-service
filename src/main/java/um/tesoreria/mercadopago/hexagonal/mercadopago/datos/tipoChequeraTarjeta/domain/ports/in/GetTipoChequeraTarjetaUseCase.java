package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;

import java.util.Optional;

public interface GetTipoChequeraTarjetaUseCase {

    Optional<TipoChequeraTarjeta> getByUnique(Integer tipoChequeraId, Integer alternativaId);

}
