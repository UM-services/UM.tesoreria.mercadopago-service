package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.domain.ports.in.GetTipoChequeraTarjetaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TipoChequeraTarjetaService {

    private final GetTipoChequeraTarjetaUseCase getTipoChequeraTarjetaUseCase;

    public Optional<TipoChequeraTarjeta> getByUnique(Integer tipoChequeraId, Integer alternativaId) {
        return getTipoChequeraTarjetaUseCase.getByUnique(tipoChequeraId, alternativaId);
    }

}
