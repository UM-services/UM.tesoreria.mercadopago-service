package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.ports.in.PublicarPagoProcesadoUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.ports.out.PublicadorPagoProcesado;

@Component
@RequiredArgsConstructor
public class PublicarPagoProcesadoUseCaseImpl implements PublicarPagoProcesadoUseCase {

    private final PublicadorPagoProcesado publicadorPagoProcesado;

    @Override
    public void publicar(PagoProcesadoEvento evento) {
        publicadorPagoProcesado.publicar(evento);
    }

}
