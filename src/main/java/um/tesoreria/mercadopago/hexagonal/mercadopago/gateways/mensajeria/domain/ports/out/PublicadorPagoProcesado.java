package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.ports.out;

import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;

public interface PublicadorPagoProcesado {

    void publicar(PagoProcesadoEvento evento);

}
