package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.ports.in;

import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;

public interface PublicarPagoProcesadoUseCase {

    void publicar(PagoProcesadoEvento evento);

}
