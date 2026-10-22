package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.ports.in.PublicarPagoProcesadoUseCase;

@Service
@RequiredArgsConstructor
public class MensajeriaService {

    private final PublicarPagoProcesadoUseCase publicarPagoProcesadoUseCase;

    public void publicar(PagoProcesadoEvento evento) {
        publicarPagoProcesadoUseCase.publicar(evento);
    }

}
